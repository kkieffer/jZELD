<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no" standalone="yes"/>

  <xsl:template match="@* | node()">
    <xsl:choose>
      <xsl:when test="local-name() != 'metadata' and local-name() != 'midPointStop'">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
