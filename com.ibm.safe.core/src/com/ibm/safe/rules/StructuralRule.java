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
package com.ibm.safe.rules;

public class StructuralRule extends IRule {

  private String query;

  private ReportLocation reportLocationQuery;

  public StructuralRule() {
    super();
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String newQuery) {
    query = newQuery;
  }

  public ReportLocation getReportLocationQuery() {
    return reportLocationQuery;
  }

  public void setReportLocationQuery(ReportLocation newReportLocationQuery) {
    reportLocationQuery = newReportLocationQuery;
  }

  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (query: ");
    result.append(query);
    result.append(", reportLocationQuery: ");
    result.append(reportLocationQuery);
    result.append(')');
    return result.toString();
  }

}
