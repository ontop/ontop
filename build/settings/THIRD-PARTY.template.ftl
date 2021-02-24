<#--
 Available context :
 - dependencyMap a collection of Map.Entry with
   key are dependencies (as a MavenProject) (from the maven project)
   values are licenses of each dependency (array of string)

 - licenseMap a collection of Map.Entry with
   key are licenses of each dependency (array of string)
   values are all dependencies using this license
-->
<#function artifactFormat p>
    <#if p.name?index_of('Unnamed') &gt; -1>
        <#return p.artifactId + " (" + p.groupId + ":" + p.artifactId + ":" + p.version + ")">
    <#else>
        <#return p.name + " (" + p.groupId + ":" + p.artifactId + ":" + p.version + ")">
    </#if>
</#function>
<#if licenseMap?size == 0>
The project has no dependencies.
<#else>
### List of third-party dependencies grouped by their license type.
    <#assign licensesetToProjects = {}>
    <#list dependencyMap as entry>
        <#assign project = entry.getKey()/>
        <#assign licenses = entry.getValue()/>
        <#assign licenseset = ""/>
        <#list licenses?sort as license>
            <#if licenseset?has_content>
                <#assign licenseset = licenseset + " or " + license/>
            <#else>
                <#assign licenseset = license/>
            </#if>
        </#list>
        <#if licensesetToProjects[licenseset]??>
            <#assign projects = licensesetToProjects[licenseset]/>
        <#else>
            <#assign projects = []/>
        </#if>
        <#assign projects = projects + [project]/>
        <#assign licensesetToProjects = licensesetToProjects + {licenseset : projects}/>
    </#list>
    <#list licensesetToProjects?keys?sort as licenseset>
        <#assign projects = licensesetToProjects[licenseset]/>

#### ${licenseset}:

        <#list projects?sort_by("name") as project>
  * ${artifactFormat(project)}
        </#list>
    </#list>
</#if>
