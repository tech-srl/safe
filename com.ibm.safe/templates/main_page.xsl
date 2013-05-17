<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html"/>

  <xsl:key name="messages-by-text" match="message" use="text"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Safe Analysis Results</title>
        <link rel="stylesheet" type="text/css" href="results.css"/>
        <script type="text/javascript" src="scripts/transformer.js"/>
        <script type="text/javascript" src="scripts/mainpage-utils.js"/>
      </head>
        
      <body onload="updateMainPage()">
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="analysis-results">    
    <h1>
      <img src="images/safe_logo_simple.jpg" style="vertical-align:middle"/>
      Global Results
    </h1>

    <xsl:variable name="nbFindings" select="count(//messages/message)"/>
    
    <xsl:if test="$nbFindings > 0">
    <table id="global-results-table" class="results">
      <thead>
        <tr>
          <td align="center">Category</td>
          <td align="center">Rule</td>
          <td align="center">Number of findings</td>
        </tr>
      </thead>

      <tbody>
        <xsl:for-each select="messages[@type='typestate']/message[generate-id(.)=generate-id(key('messages-by-text',text))]">
          <xsl:sort select="text" order="ascending"/>
          <xsl:call-template name="table-elements"/>
        </xsl:for-each>
        <xsl:if test="statistics[@solver-kind='typestate']">
         	<tr>
            <td style="font-style: italic" class="statistics">Number of classes (CHA)</td>
            <xsl:apply-templates mode="do-sum-classes" select="statistics[@solver-kind='typestate']/class-hierarchy[1]"/>
          </tr>
          
          <tr>
            <td style="font-style: italic" class="statistics">Number of methods (CHA)</td>
            <xsl:apply-templates mode="do-sum-methods" select="statistics[@solver-kind='typestate']/class-hierarchy[1]"/>
          </tr>
          
          <tr>
            <td style="font-style: italic" class="statistics">Number of bytecode statements (CHA)</td>
            <xsl:apply-templates mode="do-sum-bcstatements" select="statistics[@solver-kind='typestate']/class-hierarchy[1]"/>
          </tr>
        </xsl:if>
        <xsl:for-each select="messages[@type='structural']/message[generate-id(.)=generate-id(key('messages-by-text',text))]">
          <xsl:sort select="text" order="ascending"/>
          <xsl:call-template name="table-elements"/>
        </xsl:for-each>
        <xsl:if test="statistics[@solver-kind='structural']">
          <tr>
            <td style="font-style: italic" class="statistics">Number of classes</td>
            <td style="text-align: center" class="statistics">
              <xsl:value-of select="statistics[@solver-kind='structural']/stat[1]/value"/>
            </td>
          </tr>
          
          <tr>
            <td style="font-style: italic" class="statistics">Number of methods</td>
            <td style="text-align: center" class="statistics">
              <xsl:value-of select="statistics[@solver-kind='structural']/stat[2]/value"/>
            </td>
          </tr>
          
          <xsl:variable name="lob" select="number( statistics[@solver-kind='structural']/stat[3]/value )"/>
          <tr>
            <td style="font-style: italic" class="statistics">Number of bytecode statements</td>
            <td style="text-align: center" class="statistics">
              <xsl:value-of select="statistics[@solver-kind='structural']/stat[3]/value"/>
            </td>
          </tr>
        </xsl:if>
        
        <tr>
          <td colspan="2" style="font-style: italic" class="statistics">Number of findings</td>
          <td style="text-align: center" class="statistics">
            <xsl:value-of select="$nbFindings"/>
          </td>
        </tr>
      </tbody>
    </table>
    </xsl:if>

    <xsl:apply-templates select="timers"/>
  </xsl:template>

  <xsl:template name="table-elements">
    <tr class="{../@type}">
      <td>
        <a href='javascript:selectRuleMatches("{text}")'>
          <xsl:value-of select="text"/>
        </a>
      </td>
      <td align="center">
        <xsl:value-of select="count(key('messages-by-text',text))"/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="timers">
    <xsl:choose>
      <xsl:when test="@name='Solvers'">
        <h2>Solvers analysis time</h2>
      </xsl:when>
      <xsl:otherwise>
        <h2>Global analysis time</h2>
      </xsl:otherwise>
    </xsl:choose>

    <table id="timer-table" class="results">
      <thead>
        <tr>
          <xsl:choose>
            <xsl:when test="@name='Solvers'">
              <td>Solver kind</td>
              <xsl:variable name="timer-set" select="value!=0"/>
            </xsl:when>
            <xsl:otherwise>
              <td>Analysis kind</td>
              <xsl:variable name="timer-set" select="value!=0 and timer[not(contains(name,'Solver'))]"/>
            </xsl:otherwise>
          </xsl:choose>
          <td>Time</td>
        </tr>
      </thead>

      <tbody>
      	<xsl:choose>
          <xsl:when test="@name='Solvers'">
	          <xsl:for-each select="timer[value!=0]">
  	          <xsl:choose>
              	<xsl:when test="timeout='true'">
                	<tr>
                  	<td class="timeout"><xsl:value-of select="name"/></td>
  									<td class="timeout"><xsl:value-of select="value"/></td>
                  </tr>
                 </xsl:when>
                 <xsl:otherwise>
                 	 <tr>
                   	 <td><xsl:value-of select="name"/></td>
  									 <td><xsl:value-of select="value"/></td>
                   </tr>
                 </xsl:otherwise>
              </xsl:choose>
	          </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="timer[value!=0 and not(contains(name,'Solver'))]">
              <tr>
                <td>
                 <xsl:value-of select="name"/>
                </td>
                <td>
                  <xsl:value-of select="value"/>
                </td>
              </tr>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </tbody>
    </table>
  </xsl:template>
  
  <xsl:template mode="do-sum-classes" match="//class-hierarchy">
  	<xsl:param name="sum" select="0"/>
  	<xsl:variable name="running-sum" select="$sum + number(classes-number)"/>
  	<xsl:choose>
		<xsl:when test="following-sibling::class-hierarchy">
			<xsl:apply-templates mode="do-sum-classes" select="following-sibling::class-hierarchy[1]">
				<xsl:with-param name="sum" select="$running-sum"/>
			</xsl:apply-templates>
		</xsl:when>
	<xsl:otherwise>
		<td class="statistics" align="center"><xsl:value-of select="$running-sum"/></td>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
  <xsl:template mode="do-sum-methods" match="//class-hierarchy">
  	<xsl:param name="sum" select="0"/>
  	<xsl:variable name="running-sum" select="$sum + number(methods-number)"/>
  	<xsl:choose>
		<xsl:when test="following-sibling::class-hierarchy">
			<xsl:apply-templates mode="do-sum-methods" select="following-sibling::class-hierarchy[1]">
				<xsl:with-param name="sum" select="$running-sum"/>
			</xsl:apply-templates>
		</xsl:when>
	<xsl:otherwise>
		<td class="statistics" align="center"><xsl:value-of select="$running-sum"/></td>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
  <xsl:template mode="do-sum-bcstatements" match="//class-hierarchy">
  	<xsl:param name="sum" select="0"/>
  	<xsl:variable name="running-sum" select="$sum + number(bytecode-statements)"/>
  	<xsl:choose>
		<xsl:when test="following-sibling::class-hierarchy">
			<xsl:apply-templates mode="do-sum-bcstatements" select="following-sibling::class-hierarchy[1]">
				<xsl:with-param name="sum" select="$running-sum"/>
			</xsl:apply-templates>
		</xsl:when>
	<xsl:otherwise>
		<td class="statistics" align="center"><xsl:value-of select="$running-sum"/></td>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

</xsl:stylesheet>


