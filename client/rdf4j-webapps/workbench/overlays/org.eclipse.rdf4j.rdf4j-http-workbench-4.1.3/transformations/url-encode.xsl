<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:xl="http://www.w3.org/1999/XSL/Transform">

  <!-- ISO-8859-1 based URL-encoding demo
       Written by Mike J. Brown, mike@skew.org.
       Updated 2002-05-20.
       Updated 2022-03-14 - nguyenm100 : use divide & conquer approach to iterate through string to avoid stackoverflows
                            (see: https://stackoverflow.com/a/9081077/5237756)

       No license; use freely, but credit me if reproducing in print.

       Also see http://skew.org/xml/misc/URI-i18n/ for a discussion of
       non-ASCII characters in URIs.
  -->

  <!-- The string to URL-encode.
       Note: By "iso-string" we mean a Unicode string where all
       the characters happen to fall in the ASCII and ISO-8859-1
       ranges (32-126 and 160-255) -->
  <!--
    <xsl:param name="iso-string" select="'ciao%fax%'"/>
  -->
  <xsl:template name="url-encode">
    <xsl:param name="str"/>
    <xsl:call-template name="url-encode-dvc">
      <xsl:with-param name="str" select="$str"/>
      <xsl:with-param name="pStart" select="1"/>
      <xsl:with-param name="pEnd" select="string-length($str)"/>
    </xsl:call-template>
  </xsl:template>

  <xl:template name="process_char">
    <xsl:param name="ch"/>

    <!-- Characters we'll support.
         We could add control chars 0-31 and 127-159, but we won't. -->
    <xsl:variable name="ascii"> !"#$%&amp;'()*+,-./0123456789:;&lt;=&gt;?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
    <xsl:variable name="latin1">&#160;&#161;&#162;&#163;&#164;&#165;&#166;&#167;&#168;&#169;&#170;&#171;&#172;&#173;&#174;&#175;&#176;&#177;&#178;&#179;&#180;&#181;&#182;&#183;&#184;&#185;&#186;&#187;&#188;&#189;&#190;&#191;&#192;&#193;&#194;&#195;&#196;&#197;&#198;&#199;&#200;&#201;&#202;&#203;&#204;&#205;&#206;&#207;&#208;&#209;&#210;&#211;&#212;&#213;&#214;&#215;&#216;&#217;&#218;&#219;&#220;&#221;&#222;&#223;&#224;&#225;&#226;&#227;&#228;&#229;&#230;&#231;&#232;&#233;&#234;&#235;&#236;&#237;&#238;&#239;&#240;&#241;&#242;&#243;&#244;&#245;&#246;&#247;&#248;&#249;&#250;&#251;&#252;&#253;&#254;&#255;</xsl:variable>

    <!-- Characters that usually don't need to be escaped -->
    <xsl:variable name="safe">!'()*-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~</xsl:variable>

    <xsl:variable name="hex" >0123456789ABCDEF</xsl:variable>

    <xsl:choose>
      <xsl:when test="contains($safe,$ch)">
        <xsl:value-of select="$ch"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="codepoint">
          <xsl:choose>
            <xsl:when test="contains($ascii,$ch)">
              <xsl:value-of select="string-length(substring-before($ascii,$ch)) + 32"/>
            </xsl:when>
            <xsl:when test="contains($latin1,$ch)">
              <xsl:value-of select="string-length(substring-before($latin1,$ch)) + 160"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:message terminate="no">Warning: string contains a character that is out of range! Substituting "?".</xsl:message>
              <xsl:text>63</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="hex-digit1" select="substring($hex,floor($codepoint div 16) + 1,1)"/>
        <xsl:variable name="hex-digit2" select="substring($hex,$codepoint mod 16 + 1,1)"/>
        <xsl:value-of select="concat('%',$hex-digit1,$hex-digit2)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xl:template>

  <xsl:template name="url-encode-dvc">
    <xsl:param name="str"/>
    <xsl:param name="pStart"/>
    <xsl:param name="pEnd"/>

    <xsl:if test="not($pStart > $pEnd)">
      <xsl:choose>
        <xsl:when test="$pStart = $pEnd">
          <xsl:call-template name="process_char">
            <xsl:with-param name="ch" select="substring($str,$pStart,1)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="vMid" select=
                  "floor(($pStart + $pEnd) div 2)"/>
          <xsl:call-template name="url-encode-dvc">
            <xsl:with-param name="str" select="$str"/>
            <xsl:with-param name="pStart" select="$pStart"/>
            <xsl:with-param name="pEnd" select="$vMid"/>
          </xsl:call-template>
          <xsl:call-template name="url-encode-dvc">
            <xsl:with-param name="str" select="$str"/>
            <xsl:with-param name="pStart" select="$vMid+1"/>
            <xsl:with-param name="pEnd" select="$pEnd"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

  </xsl:template>

</xsl:stylesheet>

