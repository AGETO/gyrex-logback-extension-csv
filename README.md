Logback Extension CSV for Eclipse Gyrex
========================================

This encoder can be used with any appender that supports custom encoders. In combination with the
file appenders CSV files can be created and rotated (eg. daily).

	<appender name="FILE" class="ch.qos.logback.core.FileAppender"> 
	  <file>foo.csv</file>
	  <encoder class="net.ageto.gyrex.logback.extensions.csv.CsvEncoder">
		<!-- as many fields as you want, oder is maintained as listed here -->
		<!-- a field can be any Logback pattern (even combined strings -->
		<field>%d</field>
		<field>%level</field>
		<field>%thread</field>
		<field>%logger</field>
		<field>%msg</field>
		<field>%mdc{foo}</field>
	    <!-- this defaults to false (for better throughput) -->
		<immediateFlush>false</immediateFlush>
	  </encoder> 
	</appender>

