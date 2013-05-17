<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html"/>
  <xsl:param name="ruleName"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Safe Analysis Findings</title>
        <link rel="stylesheet" type="text/css" href="results.css"/>
        <script type="text/javascript" src="scripts/transformer.js"/>
        <script type="text/javascript" src="scripts/sorttable.js"/>
      </head>

      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="analysis-results">
    <h1>
      <img src="images/safe_logo_simple.jpg" style="vertical-align:middle"/>
      Findings
    </h1>

    <xsl:variable name="rule" select="/analysis-results/rules-matched/rule[@name=$ruleName]"/>
    <h3>
      <xsl:value-of select="$ruleName"/>
    </h3>

    <table class="categorizing">
      <tr>
        <td width="10%"></td>
        <td width="10%">
          <u>Severity:</u>
        </td>
        <td style="color:#0000EA;">
          <xsl:value-of select="$rule/severity"/>
        </td>
        <td width="12%">
          <u>Category:</u>
        </td>
        <td style="color:#0000EA;">
          <xsl:value-of select="$rule/category"/>
        </td>
      </tr>
    </table>

    <p>
      <xsl:apply-templates select="$rule/description"/>
    </p>

      <table id="table_findings" class="sortable results">
      <thead>
        <tr>
          <xsl:if test="$rule/level!='ClassLevel'">
            <td>Line</td>
          </xsl:if>
          <td>Class</td>
          <xsl:if test="$rule/level!='ClassLevel' or messages/message[text=$ruleName]/method or messages/message[text=$ruleName]/field">
            <xsl:choose>
          		<xsl:when test="messages/message[text=$ruleName]/method">
          			<td>Method</td>
          		</xsl:when>
          		<xsl:otherwise>
          			<td>Field</td>
          		</xsl:otherwise>
          	</xsl:choose>
          </xsl:if>
        </tr>
      </thead>

      <tbody>
        <xsl:apply-templates select="messages/message[text=$ruleName]">
            <xsl:with-param name="rule" select="$rule"/>
        </xsl:apply-templates>
      </tbody>
    </table>

    <h4>Example</h4>
    <div class="section">
      <xsl:apply-templates select="$rule/example"/>
    </div>

    <h4>Action</h4>
    <div class="section">
      <xsl:apply-templates select="$rule/action"/>
    </div>
  </xsl:template>

  <xsl:template match="code">
    <div class="code">
      <code>
        <xsl:apply-templates/>
      </code>
    </div>
  </xsl:template>

  <xsl:template match="b">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>

  <xsl:template match="i">
    <i>
      <xsl:value-of select="."/>
    </i>
  </xsl:template>

  <xsl:template match="em">
    <em>
      <xsl:value-of select="."/>
    </em>
  </xsl:template>

  <xsl:template match="br">
    <br/>
  </xsl:template>

  <xsl:template match="highlight">
    <span class="highlight">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

    <xsl:template match="messages/message">
    <xsl:param name="rule"/>
    <tr>
      <xsl:if test="$rule/level!='ClassLevel'">
        <xsl:choose>
          <xsl:when test="line='-1'">
            <td align="center">-</td>
          </xsl:when>
          <xsl:otherwise>
            <td align="center">
              <xsl:value-of select="line"/>
            </td>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>

      <td>
        <xsl:value-of select="class"/>
      </td>
      <xsl:if test="$rule/level!='ClassLevel' or method or field">
      	<xsl:choose>
      		<xsl:when test="method">
      			<td>
          		<xsl:value-of select="method"/>
        		</td>
      		</xsl:when>
      		<xsl:otherwise>
      			<td>
          		<xsl:value-of select="field"/>
        		</td>
      		</xsl:otherwise>
      	</xsl:choose>
      </xsl:if>
    </tr>
  </xsl:template>

</xsl:stylesheet>
