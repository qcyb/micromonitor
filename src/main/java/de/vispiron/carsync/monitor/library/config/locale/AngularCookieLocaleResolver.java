package de.vispiron.carsync.monitor.library.config.locale;

import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.*;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.util.WebUtils;

/**
 * Angular cookie saved the locale with a double quote (%22en%22). So the default
 * CookieLocaleResolver#StringUtils.parseLocaleString(localePart)
 * is not able to parse the locale.
 *
 * This class will check if a double quote has been added, if so it will remove it.
 */
public class AngularCookieLocaleResolver extends CookieLocaleResolver {

    public static final String QUOTE = "%22";

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        analyseLocaleCookieIfNecessary(request);
        return (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
    }

    @Override
    public LocaleContext resolveLocaleContext(final HttpServletRequest request) {
        analyseLocaleCookieIfNecessary(request);
        return new TimeZoneAwareLocaleContext() {
            @Override
            public Locale getLocale() {
                return (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
            }

            @Override
            public TimeZone getTimeZone() {
                return (TimeZone) request.getAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
            }
        };
    }

    @Override
    public void addCookie(HttpServletResponse response, String cookieValue) {
        // Mandatory cookie modification for AngularJS to support the locale switching on the server side.
        super.addCookie(response, quote(cookieValue));
    }

    private void analyseLocaleCookieIfNecessary(HttpServletRequest request) {
        if (request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) == null) {
            // Retrieve and parse cookie value.
            Cookie cookie = WebUtils.getCookie(request, getCookieName());
            Locale locale = null;
            TimeZone timeZone = null;
            if (cookie != null) {
                String value = cookie.getValue();

                // Remove the double quote
                value = StringUtils.replace(value, QUOTE, "");

                String localePart = value;
                String timeZonePart = null;
                int spaceIndex = localePart.indexOf(' ');
                if (spaceIndex != -1) {
                    localePart = value.substring(0, spaceIndex);
                    timeZonePart = value.substring(spaceIndex + 1);
                }
                locale = !"-".equals(localePart) ? StringUtils.parseLocaleString(localePart.replace('-', '_')) : null;
                if (timeZonePart != null) {
                    timeZone = StringUtils.parseTimeZoneString(timeZonePart);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Parsed cookie value [" + cookie.getValue() + "] into locale '" + locale +
                        "'" + (timeZone != null ? " and time zone '" + timeZone.getID() + "'" : ""));
                }
            }
            request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME,
                locale != null ? locale : determineDefaultLocale(request));

            request.setAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME,
                timeZone != null ? timeZone : determineDefaultTimeZone(request));
        }
    }

    String quote(String string) {
        return QUOTE + string + QUOTE;
    }
}
