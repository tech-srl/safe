/*******************************************************************************
 * Copyright (c) 2004-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.secure.accessibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.cha.J2SEClassHierarchyEngine;
import com.ibm.safe.reporting.message.Location;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.structural.impl.StructuralMessage;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.CodeScanner;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.Predicate;

/**
 * Given the set of classes of Application class loader's name space, the
 * <code>AccessibilityAnalyzer</code> determines if the access modifiers
 * <code>public</code>, <code>protected</code>, and <code>private</code>
 * have been used consistently. For example, if a class member is only accessed
 * from its own class, that member should be private. If it is accessed only
 * within its own package, it should be given default scope. If it is accessed
 * only within its own package, but other classes outside that package access
 * that member through subclasses of that member's class, then it should be
 * given protected scope. In all other cases, it should be given public scope.
 * For each violation, the <code>AccessibilityAnalyzer</code> reports the
 * recommended modifier that is stricter than the current one, but not too
 * strict to break the code compilation.
 * <p>
 * The <code>AccessibilityAnalyzer</code> detects also if classes or
 * interfaces in the Application class loader's name space should be given
 * default scope.
 * <p>
 * Furthermore, the <code>AccessibilityAnalyzer</code> detects if there exist
 * non-private, static fields that were not made final. In this case, the
 * <code>AccessibilityAnalyzer</code> recommends making those fields final
 * since not doing so may cause those fields to be mutated by untrusted code and
 * may allow isolation breakage (classes that are supposed to be isolated
 * communicate with each other by setting and getting values of static,
 * non-final fields in name spaces of ancestor class loaders).
 * 
 * @author Marco Pistoia
 */
public class AccessibilityAnalyzer implements AccessibilityConstants {

  private final boolean DEBUG = false;

  private List<IClass> classes = new ArrayList<IClass>();

  public AccessibilityAnalyzer() {
  }

  /**
   * Perform the accessibility analysis.
   * 
   * @param cha
   *            The ClassHierarchy object for the analysis scope.
   * @param rules
   *            The IStructuralRules representing the accessibility rule for
   *            this analysis
   * @return a Set of Message objects, each Message object representing a
   *         violation of the <code>rule</code>
   */
  public Set<StructuralMessage> process(IClassHierarchy cha, StructuralRule[] rules, final Predicate<IClass> classFilter) {
    Map<AccessibilityTarget, ArrayList<Accessor>> targetsToAccessors = computeAccessibility(cha, classFilter);
    if (DEBUG)
      printAccessibility(targetsToAccessors);
    Map<Integer, StructuralRule> accessibilityRulesMap = getAccessibilityRules(rules);
    return computeMemberViolations(targetsToAccessors, accessibilityRulesMap, cha);
  }

  /**
   * For debug purposes, this method prints, for each target member, the set of
   * methods accessing it.
   */
  private void printAccessibility(Map<AccessibilityTarget, ArrayList<Accessor>> targetsToAccessors) {
    Iterator<Map.Entry<AccessibilityTarget, ArrayList<Accessor>>> targetsToAccessorsIter = targetsToAccessors.entrySet().iterator();
    while (targetsToAccessorsIter.hasNext()) {
      Map.Entry<AccessibilityTarget, ArrayList<Accessor>> entry = targetsToAccessorsIter.next();
      AccessibilityTarget target = (AccessibilityTarget) entry.getKey();
      System.out.println(target);
      Iterator<Accessor> accIter = ((List<Accessor>) entry.getValue()).iterator();
      while (accIter.hasNext()) {
        AccessibilityMember accessor = (AccessibilityMember) accIter.next();
        System.out.println(accessor);
      }
      System.out.println("=======================");
    }
  }

  /**
   * For each target member, this method computes if the access modifier chosen
   * for that member is appropriate or should have been made more restrictive.
   * The computation is based on all the methods that access that member.
   * 
   * @param accessibilityRulesMap
   * @param cha
   * @return a Set of Message objects, each of which represents an access
   *         control violation.
   */
  private Set<StructuralMessage> computeMemberViolations(Map<AccessibilityTarget, ArrayList<Accessor>> targetsToAccessors,
      Map<Integer, StructuralRule> accessibilityRulesMap, IClassHierarchy cha) {
    Set<StructuralMessage> messages = new HashSet<StructuralMessage>();
    Iterator<Map.Entry<AccessibilityTarget, ArrayList<Accessor>>> targetsToAccessorsIter = targetsToAccessors.entrySet().iterator();
    while (targetsToAccessorsIter.hasNext()) {
      Map.Entry<AccessibilityTarget, ArrayList<Accessor>> entry = targetsToAccessorsIter.next();
      AccessibilityTarget target = (AccessibilityTarget) entry.getKey();

      // Detect if the given target represents a non-private, non-final, static
      // field
      if (target.isField() && target.isStatic() && !target.isFinal()) {
        Location location = Location.createFieldLocation(target.getClassName(), target.getMemberName());
        StructuralRule rule = accessibilityRulesMap.get(FIELD_MAKE_FINAL_ID);
        messages.add(new AccessibilityMessage(rule, location, target));
      }

      int currentModifier = target.getCurrentModifier();
      List<Accessor> accessors = (List<Accessor>) entry.getValue();
      if (accessors.isEmpty() && !target.getMemberName().endsWith("<clinit>()V")) {
        Location location = Location.createMethodLocation(target.getClassName(), target.getMemberName());
        StructuralRule rule = null;
        if (target.isField()) {
          rule = accessibilityRulesMap.get(FIELD_UNREFERENCED_ID);
        } else { // target is a method
          if (currentModifier == PUBLIC) {
            rule = accessibilityRulesMap.get(PUBLIC_METHOD_UNREACHABLE_ID);
          } else if (currentModifier == PROTECTED) {
            rule = accessibilityRulesMap.get(PROTECTED_METHOD_UNREACHABLE_ID);
          } else if (currentModifier == DEFAULT) {
            rule = accessibilityRulesMap.get(DEFAULT_METHOD_UNREACHABLE_ID);
          } else {
            rule = accessibilityRulesMap.get(PRIVATE_METHOD_UNREACHABLE_ID);
          }
        }
        messages.add(new AccessibilityMessage(rule, location, target));
        continue;
      }

      // Private scope: nothing to do
      if (currentModifier == PRIVATE) {
        target.setSuggestedModifier(PRIVATE);
        continue; // Nothing can be better than private!
      }

      int superModifier = target.getSuperModifier();
      if (superModifier == currentModifier) {
        target.setSuggestedModifier(currentModifier);
        continue;
      }
      Iterator<Accessor> accessorsIter = accessors.iterator();
      // Default scope: see if we can make it private
      if (currentModifier == DEFAULT) {
        boolean okToBeDefault = false; // assume that it is not OK for this
        // member to have default scope
        while (accessorsIter.hasNext()) {
          Accessor accessor = accessorsIter.next();
          // If the class to which the target member belongs is not the same as
          // the class of the accessor
          if (!target.getClassLoaderName().equals(accessor.getClassLoaderName())
              || !target.getClassName().equals(accessor.getClassName())
              || !target.getClassLoaderName().equals(accessor.getAccessingClassLoaderName())
              || !target.getClassName().equals(accessor.getAccessingClassName())) {
            okToBeDefault = true;
            target.setSuggestedModifier(DEFAULT);
            break;
          }
          if (!okToBeDefault) {
            target.setSuggestedModifier(PRIVATE);
            Location location;
            StructuralRule rule;
            if (target.isField()) {
              location = Location.createFieldLocation(target.getClassName(), target.getMemberName());
              rule = accessibilityRulesMap.get(DEFAULT_FIELD_PRIVATE_ID);
            } else {
              location = Location.createMethodLocation(target.getClassName(), target.getMemberName());
              rule = accessibilityRulesMap.get(DEFAULT_METHOD_PRIVATE_ID);
            }
            messages.add(new AccessibilityMessage(rule, location, target));
            if (DEBUG)
              System.out.println(target + MAKE_PRIVATE);
          }
        }
        continue;
      }

      // Protected scope: see if we can make it default or private
      if (currentModifier == PROTECTED) {
        boolean okNotToBePrivate = false;
        boolean okNotToBeDefault = false;
        while (accessorsIter.hasNext()) {
          Accessor accessor = (Accessor) accessorsIter.next();
          if (!target.getClassLoaderName().equals(accessor.getClassLoaderName())
              || !target.getClassLoaderName().equals(accessor.getAccessingClassLoaderName())) {
            // The protected modifier was appropriate.
            okNotToBePrivate = true;
            okNotToBeDefault = true;
            target.setSuggestedModifier(PROTECTED);
            break;
          }
          if (!target.getClassName().equals(accessor.getClassName())
              || !target.getClassName().equals(accessor.getAccessingClassName())) {
            // The private modifier would have been too restrictive.
            // Either default of protected is appropriate.
            okNotToBePrivate = true;
          }
          if (!target.getPackageName().equals(accessor.getPackageName())
              || !target.getPackageName().equals(accessor.getAccessingPackageName())) {
            okNotToBeDefault = true;
            target.setSuggestedModifier(PROTECTED);
            break;
          }
        }
        if (!okNotToBeDefault) {
          Location location;
          if (target.isField()) {
            location = Location.createFieldLocation(target.getClassName(), target.getMemberName());
          } else {
            location = Location.createMethodLocation(target.getClassName(), target.getMemberName());
          }
          StructuralRule rule;
          if (!okNotToBePrivate && currentModifier > superModifier) {
            target.setSuggestedModifier(PRIVATE);
            if (target.isField()) {
              rule = accessibilityRulesMap.get(PROTECTED_FIELD_PRIVATE_ID);
            } else {
              rule = accessibilityRulesMap.get(PROTECTED_METHOD_PRIVATE_ID);
            }
            messages.add(new AccessibilityMessage(rule, location, target));
            if (DEBUG)
              System.out.println(target + MAKE_PRIVATE);
          } else {
            target.setSuggestedModifier(DEFAULT);
            if (target.isField()) {
              rule = accessibilityRulesMap.get(PROTECTED_FIELD_DEFAULT_ID);
            } else {
              rule = accessibilityRulesMap.get(PROTECTED_METHOD_DEFAULT_ID);
            }
            messages.add(new AccessibilityMessage(rule, location, target));
            if (DEBUG)
              System.out.println(target + MAKE_DEFAULT);
          }
        }
        continue;
      }

      // Public scope: see if we can make it protected, default, or private
      if (currentModifier == PUBLIC) {
        boolean okNotToBePrivate = false;
        boolean okNotToBeDefault = false;
        boolean okNotToBeProtected = false;
        while (accessorsIter.hasNext()) {
          Accessor accessor = (Accessor) accessorsIter.next();
          if (!target.getClassLoaderName().equals(accessor.getClassLoaderName())
              || !target.getClassLoaderName().equals(accessor.getAccessingClassLoaderName())) {
            okNotToBePrivate = true;
            okNotToBeDefault = true;
          } else {
            if (!target.getClassName().equals(accessor.getClassName())
                || !target.getClassName().equals(accessor.getAccessingClassName())) {
              okNotToBePrivate = true;
            }
            if (!target.getPackageName().equals(accessor.getPackageName())
                || !target.getPackageName().equals(accessor.getAccessingPackageName())) {
              okNotToBeDefault = true;
            }
          }
          if (!target.getPackageName().equals(accessor.getPackageName())
              && !accessor.getClassName().equals(accessor.getAccessingClassName())) {
            okNotToBeProtected = true;
            target.setSuggestedModifier(PUBLIC);
            break;
          }
        }

        if (!okNotToBeProtected && currentModifier > superModifier) {
          Location location;
          if (target.isField()) {
            location = Location.createFieldLocation(target.getClassName(), target.getMemberName());
          } else {
            location = Location.createMethodLocation(target.getClassName(), target.getMemberName());
          }
          if (!okNotToBeDefault) {
            if (!okNotToBePrivate) {
              target.setSuggestedModifier(PRIVATE);
              StructuralRule rule;
              if (target.isField()) {
                rule = accessibilityRulesMap.get(PUBLIC_FIELD_PRIVATE_ID);
              } else {
                rule = accessibilityRulesMap.get(PUBLIC_METHOD_PRIVATE_ID);
              }
              messages.add(new AccessibilityMessage(rule, location, target));
              if (DEBUG)
                System.out.println(target + MAKE_PRIVATE);
            } else {
              target.setSuggestedModifier(DEFAULT);
              StructuralRule rule;
              if (target.isField()) {
                rule = accessibilityRulesMap.get(PUBLIC_FIELD_DEFAULT_ID);
              } else {
                rule = accessibilityRulesMap.get(PUBLIC_METHOD_DEFAULT_ID);
              }
              messages.add(new AccessibilityMessage(rule, location, target));
              if (DEBUG)
                System.out.println(target + MAKE_DEFAULT);
            }
          } else {
            target.setSuggestedModifier(PROTECTED);
            StructuralRule rule;
            if (target.isField()) {
              rule = accessibilityRulesMap.get(PUBLIC_FIELD_PROTECTED_ID);
            } else {
              rule = accessibilityRulesMap.get(PUBLIC_METHOD_PROTECTED_ID);
            }
            messages.add(new AccessibilityMessage(rule, location, target));
            if (DEBUG)
              System.out.println(target + MAKE_PROTECTED);
          }
        }
      }
    }

    Iterator<IClass> classesIter = classes.iterator();
    while (classesIter.hasNext()) {
      IClass klass = classesIter.next();
      if (Modifier.isDefault(klass.getModifiers())) {
        continue; // Nothing better to do
      }
      String thisPackageName = AccessibilityMember.computePackageName(klass.getName().toString());
      Iterator<IClass> subclasses = cha.getImmediateSubclasses(klass).iterator();
      boolean okToBePublic = false;
      while (subclasses.hasNext()) {
        IClass subclass = subclasses.next();
        String thatPackageName = AccessibilityMember.computePackageName(subclass.getName().toString());
        if (!thisPackageName.equals(thatPackageName)) {
          okToBePublic = true;
          break; // Nothing to do here
        }
      }
      if (!okToBePublic) {
        Iterator<IField> instanceFields = klass.getDeclaredInstanceFields().iterator();
        while (instanceFields.hasNext()) {
          IField instanceField = (IField) instanceFields.next();
          AccessibilityTarget target = AccessibilityTarget.getAccessibilityTarget(instanceField);
          int currentMod = target.getCurrentModifier();
          if (currentMod >= PROTECTED) {
            okToBePublic = true;
            break;
          }
        }
        Iterator<IField> staticFields = klass.getDeclaredStaticFields().iterator();
        while (staticFields.hasNext()) {
          IField staticField = (IField) staticFields.next();
          AccessibilityTarget target = AccessibilityTarget.getAccessibilityTarget(staticField);
          int currentMod = target.getCurrentModifier();
          if (currentMod >= PROTECTED) {
            okToBePublic = true;
            break;
          }
        }
        Iterator<IMethod> methods = klass.getDeclaredMethods().iterator();
        while (methods.hasNext()) {
          IMethod method = (IMethod) methods.next();
          AccessibilityTarget target = AccessibilityTarget.getAccessibilityTarget(method);
          int currentMod = target.getCurrentModifier();
          if (currentMod >= PROTECTED && !(target.getMemberName().endsWith("<init>()V") && currentMod == PUBLIC)) {
            okToBePublic = true;
            break;
          }
        }
      }
      if (!okToBePublic) {
        Location location = Location.createClassLocation(klass.getName().toString(), -1);
        StructuralRule rule = accessibilityRulesMap.get(PUBLIC_CLASS_DEFAULT_ID);
        messages.add(new StructuralMessage(rule, location));
      }
    }
    return messages;
  }

  /**
   * Computes how each class, interface, and member in the analysis scope gets
   * accessed. Each class, interface, and member is mapped to the the set of its
   * accessors.
   * 
   * @param cha
   *            A ClassHierarchy object representing the class hierarchy of all
   *            the classes in the analysis scope.
   */
  private Map<AccessibilityTarget, ArrayList<Accessor>> computeAccessibility(final IClassHierarchy cha, final Predicate<IClass> classFilter) {
    Map<AccessibilityTarget, ArrayList<Accessor>> targetsToAccessors = new HashMap<AccessibilityTarget, ArrayList<Accessor>>();
    IClassLoader appLoader = cha.getLoader(ClassLoaderReference.Application);
    Iterator<IClass> appClassesIter = appLoader.iterateAllClasses();
    while (appClassesIter.hasNext()) {
      IClass klass = appClassesIter.next();
      if (!classFilter.test(klass)) {
        continue;
      }
      classes.add(klass);
      if (klass.isInterface()) {
        continue;
      }
      Iterator<IField> fieldsIter = klass.getAllFields().iterator();
      while (fieldsIter.hasNext()) {
        IField field = fieldsIter.next();
        if (!field.getDeclaringClass().getClassLoader().equals(appLoader) || field.getName().toString().indexOf('$') > -1)
          continue;
        AccessibilityTarget target = AccessibilityTarget.getAccessibilityTarget(field);
        targetsToAccessors.put(target, new ArrayList<Accessor>(3));
      }
      Iterator<IMethod> methodsIter = klass.getAllMethods().iterator();
      while (methodsIter.hasNext()) {
        IMethod method = methodsIter.next();
        if (method.isAbstract() || !method.getDeclaringClass().getClassLoader().equals(appLoader)
            || method.getName().toString().indexOf('$') > -1)
          continue;
        AccessibilityTarget target = AccessibilityTarget.getAccessibilityTarget(method);
        targetsToAccessors.put(target, new ArrayList<Accessor>(3));
      }
    }

    if (DEBUG) {
      int numClasses = cha.getNumberOfClasses();
      Trace.println("Number of classes:" + numClasses);
    }
    for (IClass klass : cha) {
      if (!J2SEClassHierarchyEngine.isApplicationClass(klass)) {
        continue;
      }
      Iterator<IMethod> methodsIter = klass.getDeclaredMethods().iterator();
      while (methodsIter.hasNext()) {
        IMethod method = methodsIter.next();
        try {
          Iterator<FieldReference> readFieldRefIter = CodeScanner.getFieldsRead(method).iterator();
          while (readFieldRefIter.hasNext()) {
            FieldReference fieldRef = readFieldRefIter.next();
            if (!fieldRef.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application)
                || fieldRef.getName().toString().indexOf('$') > -1)
              continue;
            IField iField = appLoader.lookupClass(fieldRef.getDeclaringClass().getName()).getField(fieldRef.getName());
            if (!iField.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application)
                || iField.getName().toString().indexOf('$') > -1)
              continue;
            AccessibilityTarget target = AccessibilityTarget.getAccessibilityTarget(iField);
            List<Accessor> accessors = targetsToAccessors.get(target);
            accessors.add(new Accessor(method, fieldRef.getDeclaringClass().getName().toString(), fieldRef.getDeclaringClass()
                .getClassLoader().getName().toString()));
          }
          Iterator<FieldReference> writtenFieldRefIter = CodeScanner.getFieldsWritten(method).iterator();
          while (writtenFieldRefIter.hasNext()) {
            FieldReference fieldRef = writtenFieldRefIter.next();
            if (!fieldRef.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application)
                || fieldRef.getName().toString().indexOf('$') > -1)
              continue;
            IField iField = appLoader.lookupClass(fieldRef.getDeclaringClass().getName()).getField(fieldRef.getName());
            if (!iField.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application)
                || iField.getName().toString().indexOf('$') > -1)
              continue;
            AccessibilityTarget target = AccessibilityTarget.getAccessibilityTarget(iField);
            List<Accessor> accessors = targetsToAccessors.get(target);
            accessors.add(new Accessor(method, fieldRef.getDeclaringClass().getName().toString(), fieldRef.getDeclaringClass()
                .getClassLoader().getName().toString()));
          }
          Iterator<CallSiteReference> callSitesIter = CodeScanner.getCallSites(method).iterator();
          while (callSitesIter.hasNext()) {
            CallSiteReference callSiteRef = callSitesIter.next();
            MethodReference methodRef = callSiteRef.getDeclaredTarget();
            if (!methodRef.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application)
                || methodRef.getName().toString().indexOf('$') > -1)
              continue;
            IMethod iMethod = appLoader.lookupClass(methodRef.getDeclaringClass().getName()).getMethod(methodRef.getSelector());
            IClass iClass = iMethod.getDeclaringClass();

            if (!J2SEClassHierarchyEngine.isApplicationClass(iClass) || iMethod.getName().toString().indexOf('$') > -1)
              continue;

            int superModifier = -1;
            int modifier;
            if (iMethod.isPublic()) {
              modifier = PUBLIC;
            } else if (iMethod.isProtected()) {
              modifier = PROTECTED;
            } else if (iMethod.isPrivate()) {
              modifier = PRIVATE;
            } else {
              modifier = DEFAULT;
            }
            IClass superClass = iClass.getSuperclass();
            if (superClass != null) {
              IMethod superMethod = superClass.getMethod(iMethod.getSelector());
              if (superMethod != null) {
                if (superMethod.isPublic()) {
                  superModifier = PUBLIC;
                } else if (superMethod.isProtected()) {
                  superModifier = PROTECTED;
                } else if (superMethod.isPrivate()) {
                  superModifier = PRIVATE;
                } else {
                  superModifier = DEFAULT;
                }
              }
            }
            if (modifier > superModifier) {
              // try to see if iMethod overrides a method in an interface
              Collection<IClass> interfaces;

              interfaces = iClass.getAllImplementedInterfaces();
              Iterator<IClass> interfacesIter = interfaces.iterator();
              while (interfacesIter.hasNext()) {
                IClass intf = interfacesIter.next();
                IMethod intfMethod = intf.getMethod(iMethod.getSelector());
                if (intfMethod == null)
                  continue;
                int intfModifier = -1;
                if (intfMethod.isPublic()) {
                  intfModifier = PUBLIC;
                } else if (intfMethod.isProtected()) {
                  intfModifier = PROTECTED;
                } else if (intfMethod.isPrivate()) {
                  intfModifier = PRIVATE;
                } else {
                  intfModifier = DEFAULT;
                }
                superModifier = Math.max(superModifier, intfModifier);
                if (modifier == superModifier) {
                  // The modifier of iMethod cannot be restricted
                  break;
                }
              }
            }

            AccessibilityTarget target = AccessibilityTarget.getAccessibilityTarget(iMethod);
            target.setSuperModifier(superModifier);
            List<Accessor> accessors = targetsToAccessors.get(target);
            accessors.add(new Accessor(method, methodRef.getDeclaringClass().getName().toString(), methodRef.getDeclaringClass()
                .getClassLoader().getName().toString()));
          }
        } catch (InvalidClassFileException ice) {
          ice.printStackTrace();
        } catch (NullPointerException npe) {
        }
      }
    }
    return targetsToAccessors;
  }

  private Map<Integer, StructuralRule> getAccessibilityRules(StructuralRule[] rules) {
    Map<Integer, StructuralRule> accessibilityRulesMap = new HashMap<Integer, StructuralRule>();
    for (int i = 0; i < rules.length; i++) {
      if (rules[i].getName().equals(PUBLIC_FIELD_PROTECTED)) {
        accessibilityRulesMap.put(PUBLIC_FIELD_PROTECTED_ID, rules[i]);
      } else if (rules[i].getName().equals(PUBLIC_FIELD_DEFAULT)) {
        accessibilityRulesMap.put(PUBLIC_FIELD_DEFAULT_ID, rules[i]);
      } else if (rules[i].getName().equals(PUBLIC_FIELD_PRIVATE)) {
        accessibilityRulesMap.put(PUBLIC_FIELD_PRIVATE_ID, rules[i]);
      } else if (rules[i].getName().equals(PROTECTED_FIELD_DEFAULT)) {
        accessibilityRulesMap.put(PROTECTED_FIELD_DEFAULT_ID, rules[i]);
      } else if (rules[i].getName().equals(PROTECTED_FIELD_PRIVATE)) {
        accessibilityRulesMap.put(PROTECTED_FIELD_PRIVATE_ID, rules[i]);
      } else if (rules[i].getName().equals(DEFAULT_FIELD_PRIVATE)) {
        accessibilityRulesMap.put(DEFAULT_FIELD_PRIVATE_ID, rules[i]);
      } else if (rules[i].getName().equals(PUBLIC_METHOD_PROTECTED)) {
        accessibilityRulesMap.put(PUBLIC_METHOD_PROTECTED_ID, rules[i]);
      } else if (rules[i].getName().equals(PUBLIC_METHOD_DEFAULT)) {
        accessibilityRulesMap.put(PUBLIC_METHOD_DEFAULT_ID, rules[i]);
      } else if (rules[i].getName().equals(PUBLIC_METHOD_PRIVATE)) {
        accessibilityRulesMap.put(PUBLIC_METHOD_PRIVATE_ID, rules[i]);
      } else if (rules[i].getName().equals(PROTECTED_METHOD_DEFAULT)) {
        accessibilityRulesMap.put(PROTECTED_METHOD_DEFAULT_ID, rules[i]);
      } else if (rules[i].getName().equals(PROTECTED_METHOD_PRIVATE)) {
        accessibilityRulesMap.put(PROTECTED_METHOD_PRIVATE_ID, rules[i]);
      } else if (rules[i].getName().equals(DEFAULT_METHOD_PRIVATE)) {
        accessibilityRulesMap.put(DEFAULT_METHOD_PRIVATE_ID, rules[i]);
      } else if (rules[i].getName().equals(FIELD_UNREFERENCED)) {
        accessibilityRulesMap.put(FIELD_UNREFERENCED_ID, rules[i]);
      } else if (rules[i].getName().equals(PUBLIC_METHOD_UNREACHABLE)) {
        accessibilityRulesMap.put(PUBLIC_METHOD_UNREACHABLE_ID, rules[i]);
      } else if (rules[i].getName().equals(PROTECTED_METHOD_UNREACHABLE)) {
        accessibilityRulesMap.put(PROTECTED_METHOD_UNREACHABLE_ID, rules[i]);
      } else if (rules[i].getName().equals(DEFAULT_METHOD_UNREACHABLE)) {
        accessibilityRulesMap.put(DEFAULT_METHOD_UNREACHABLE_ID, rules[i]);
      } else if (rules[i].getName().equals(PRIVATE_METHOD_UNREACHABLE)) {
        accessibilityRulesMap.put(PRIVATE_METHOD_UNREACHABLE_ID, rules[i]);
      } else if (rules[i].getName().equals(FIELD_MAKE_FINAL)) {
        accessibilityRulesMap.put(FIELD_MAKE_FINAL_ID, rules[i]);
      } else if (rules[i].getName().equals(PUBLIC_CLASS_DEFAULT)) {
        accessibilityRulesMap.put(PUBLIC_CLASS_DEFAULT_ID, rules[i]);
      } 
    }
    return accessibilityRulesMap;
  }
}