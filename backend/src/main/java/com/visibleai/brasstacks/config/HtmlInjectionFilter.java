package com.visibleai.brasstacks.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Injects common scripts (RUM, analytics) into the &lt;head&gt; of every HTML response.
 * This avoids duplicating the snippet across every static HTML file.
 */
@Component
public class HtmlInjectionFilter implements Filter {

    private static final String HEAD_INJECTION = """
            <!-- VerOps RUM SDK v2 -->
            <script src="https://app.verops.io/cdn/rum-sdk/v2/verops-rum.min.js" crossorigin="anonymous" defer></script>
            <script>
              document.addEventListener('DOMContentLoaded', function () {
                window.VeropsRUM && window.VeropsRUM.init({
                  appId: 'rum_fec90b578874438a',
                  apiKey: 'rum_pk_82uuMponG6KUKX8UJLQXl7W480xqV666XXqExYRjIGY',
                  endpoint: 'https://ingest.verops.io/rum/v2/beacon',
                  environment: 'prod',
                  respectDoNotTrack: true,
                });
              });
            </script>
            """;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap the response to capture the output
        CapturingResponseWrapper wrapper = new CapturingResponseWrapper(httpResponse);
        chain.doFilter(request, wrapper);

        String contentType = wrapper.getContentType();
        byte[] content = wrapper.getCapturedBytes();

        // Only inject into HTML responses
        if (contentType != null && contentType.contains("text/html") && content.length > 0) {
            String html = new String(content, StandardCharsets.UTF_8);

            // Remove any existing hardcoded RUM snippets
            html = html.replaceAll("(?s)<!-- VerOps RUM SDK v2 -->.*?</script>\\s*</script>", "");
            // Also clean up the simpler pattern
            html = html.replaceAll("(?s)<!-- VerOps RUM SDK v2 -->[\\s\\S]*?VeropsRUM\\.init\\([\\s\\S]*?\\);[\\s\\S]*?</script>", "");

            // Inject before </head>
            html = html.replace("</head>", HEAD_INJECTION + "</head>");

            byte[] modified = html.getBytes(StandardCharsets.UTF_8);
            httpResponse.setContentLength(modified.length);
            httpResponse.getOutputStream().write(modified);
        } else {
            // Pass through non-HTML responses unchanged
            if (content.length > 0) {
                httpResponse.getOutputStream().write(content);
            }
        }
    }

    /**
     * Response wrapper that captures output into a byte array instead of
     * writing directly to the client.
     */
    private static class CapturingResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
        private ServletOutputStream outputStream;
        private PrintWriter writer;

        public CapturingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (outputStream == null) {
                outputStream = new ServletOutputStream() {
                    @Override public void write(int b) { capture.write(b); }
                    @Override public void write(byte[] b, int off, int len) { capture.write(b, off, len); }
                    @Override public boolean isReady() { return true; }
                    @Override public void setWriteListener(WriteListener listener) {}
                };
            }
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(capture, StandardCharsets.UTF_8), true);
            }
            return writer;
        }

        public byte[] getCapturedBytes() {
            if (writer != null) writer.flush();
            return capture.toByteArray();
        }
    }
}
