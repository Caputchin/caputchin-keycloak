<#--
  Renders the Caputchin verification element from the attributes the extension exposes on the form
  context. Override this single file in your own theme (or render the element yourself) to change how
  the widget looks; the rest of the contract (the caputchin* attributes and the caputchin-token field)
  stays the same.

  Style it with CSS parts and custom properties, for example:
    caputchin-widget::part(simple-checkbox) { ... }
    caputchin-widget { --cpt-skin-primary: #2F6640; }
-->
<#macro widget>
  <#if (caputchinRequired!false)>
    <#assign cpLocale = caputchinLocale!'kc'>
    <#if cpLocale == 'kc'>
      <#assign cpLocale = (locale.currentLanguageTag)!''>
    </#if>
    <div class="caputchin-field" id="caputchin-field" style="margin: 1rem 0;">
      <#if (caputchinIsGame!false)>
        <caputchin-game sitekey="${caputchinSiteKey!''}"<#if (caputchinGame!'')?has_content> game="${caputchinGame}"</#if><#if (caputchinGames!'')?has_content> games="${caputchinGames}"</#if><#if (caputchinGameSrc!'')?has_content> game-src="${caputchinGameSrc}"</#if> layout="${caputchinLayout!'auto'}"<#if cpLocale?has_content> locale="${cpLocale}"</#if> skin="${caputchinSkin!'auto'}"<#if (caputchinApiHost!'')?has_content> api-host="${caputchinApiHost}"</#if>></caputchin-game>
      <#else>
        <caputchin-widget sitekey="${caputchinSiteKey!''}" trigger="form-submit"<#if (caputchinInvisible!false)> invisible</#if> size="${caputchinSize!'normal'}"<#if cpLocale?has_content> locale="${cpLocale}"</#if> skin="${caputchinSkin!'auto'}"<#if (caputchinApiHost!'')?has_content> api-host="${caputchinApiHost}"</#if>></caputchin-widget>
      </#if>
    </div>
    <script src="${caputchinLoaderSrc}" async defer></script>
  </#if>
</#macro>
