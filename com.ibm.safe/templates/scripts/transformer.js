function selectRuleMatches( ruleName ) {
  if ( document.implementation && document.implementation.createDocument ) {
    var xmldoc = document.implementation.createDocument( "", "", null );
    var newWindow = window.open( "findings.htm", "_self", "resizable=yes,scrollbars=yes" );
    xmldoc.onload = function() {
      var xsl = document.implementation.createDocument( "xsl", "stylesheet", null );
      xsl.onload = function() {
        xsltProcessor = new XSLTProcessor();
        xsltProcessor.importStylesheet( xsl );

        xsltProcessor.setParameter( null, "ruleName", ruleName );
        var fragment = xsltProcessor.transformToFragment( xmldoc, document );
        newWindow.document.body.appendChild( fragment );
      };
      xsl.load( "findings.xsl" );
    }
    xmldoc.load( document.location.href );
  } else if ( window.ActiveXObject ) { 
    var xmldoc = new ActiveXObject( "Microsoft.XMLDOM" );
    xmldoc.async = "false";
    var newWindow = window.open( "findings.htm", "_self", "resizable=yes,scrollbars=yes" );
    xmldoc.onreadystatechange = function() {
      if ( xmldoc.readyState == 4 ) {
        var xsl = new ActiveXObject( "Microsoft.XMLDOM" );
        xsl.async = "false";
        xsl.onreadystatechange = function() {
          if ( xsl.readyState == 4 ) {
            xsl.selectSingleNode( '//xsl:param[@name="ruleName"]' ).setAttribute( "select", "\"" + ruleName + "\"" );
            newWindow.document.write( xmldoc.transformNode( xsl ) );
          }
        }
        xsl.load( "findings.xsl" );
      }
    }
    xmldoc.load( document.location.href );
  }
}