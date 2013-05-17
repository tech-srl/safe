function updateMainPage() {
  var tables = document.getElementsByTagName( "table" );
  for( var i = 0; i < tables.length; ++i ) {
    var totalTime = 0;
    if ( ( tables[ i ].id == null ) || ( tables[ i ].id != "timer-table" ) ) continue;
    
    for( var index = 1; index < tables[ i ].rows.length; ++index ) {
      var cell = parseInt( tables[ i ].rows[ index ].cells[ 1 ].firstChild.data );
      totalTime += cell;
      var date = new Date( cell );
      tables[ i ].rows[ index ].cells[ 1 ].firstChild.data = date.getMinutes() + " mn " + date.getSeconds() + " secs";
    }
    if ( tables[ i ].rows.length > 2 ) {
      var row = tables[ i ].insertRow( -1 );
      row.bgColor = '#0000EE';
      row.insertCell( 0 ).innerHTML = "<i>Total</i>";
      row.cells[ 0 ].style.backgroundColor = '#C0C0C0';
      var date = new Date( totalTime );
      row.insertCell( 1 ).innerHTML = date.getMinutes() + " mn " + date.getSeconds() + " secs";
      row.cells[ 1 ].style.backgroundColor = '#C0C0C0';
    }
  }
  
  for( var i = 0; i < tables.length; ++i ) {
    if ( ( tables[ i ].id == null ) || ( tables[ i ].id != "global-results-table" ) ) continue;

    var globalTable = tables[ i ];
    var nbTypeStateMatches = 0;
    var nbStructuralMatches = 0;
    var nbFindings = 0;
    var middleFrontierIndex = 0;
    for( var index = 1; index < globalTable.rows.length; ++index ) {
      if ( globalTable.rows[ index ].className == 'typestate' ) {
        ++nbTypeStateMatches;
        nbFindings += parseInt( globalTable.rows[ index ].cells[ 1 ].firstChild.data );
      } else if ( globalTable.rows[ index ].className == 'structural' ) {
        ++nbStructuralMatches;
        if ( middleFrontierIndex == 0 ) {
          middleFrontierIndex = index;
        }
        nbFindings += parseInt( globalTable.rows[ index ].cells[ 1 ].firstChild.data );
      }
    }
    if ( middleFrontierIndex - 1 != nbTypeStateMatches ) {
      nbTypeStateMatches = middleFrontierIndex - nbTypeStateMatches;
    }
    if ( nbTypeStateMatches != 0 ) {
      var cell = globalTable.rows[ 1 ].insertCell( 0 );
      cell.innerHTML = "TypeState";
      cell.rowSpan = nbTypeStateMatches;
    }
    if ( nbStructuralMatches != 0 ) {
      var cell = globalTable.rows[ 1 + nbTypeStateMatches ].insertCell( 0 );
      cell.innerHTML = "Structural";
      cell.rowSpan = globalTable.rows.length - 2 - middleFrontierIndex + 1;
    }
  }
}

function getPercentage( value ) {
  return ( ( value + "0" ).match( /.*\.\d{2}/ ) || value + ".00" ) + " %";
}