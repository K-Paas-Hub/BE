package com.app.recruit.util.etc;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 사람인 공고 상세 크롤러 (Ajax 기반, 섹션별 추출 + 이미지 본문 대응 + 항목 이름 변형 대응)
 */
public class JobCrawler {

    public static class JobDetail {
        public String name;
        public String recIdx;
        public List<String> tasks;        // 주요업무/담당업무
        public List<String> requirements; // 자격요건/지원자격
        public List<String> preferred;    // 우대사항/우대조건
        public List<String> conditions;   // 근무조건/고용형태
        public List<String> procedure;    // 채용절차/전형절차
        public List<String> detailImages;
        public String welfare;            // JSON 문자열
        public String workAddress;
        public String subway;

        public String[] toCsvRow() {
            return new String[]{
                    name != null ? name : "",
                    tasks != null ? String.join(" | ", tasks) : "",
                    requirements != null ? String.join(" | ", requirements) : "",
                    preferred != null ? String.join(" | ", preferred) : "",
                    conditions != null ? String.join(" | ", conditions) : "",
                    procedure != null ? String.join(" | ", procedure) : "",
                    detailImages != null ? String.join(" ", detailImages) : "",
                    welfare != null ? welfare : "",
                    workAddress != null ? workAddress : "",
                    subway != null ? subway : ""
            };
        }

        @Override
        public String toString() {
            return String.format(
                    "공고명: %s\nrec_idx: %s\n주요업무: %s\n자격요건: %s\n우대사항: %s\n근무조건: %s\n채용절차: %s\n이미지: %s\n복리후생: %s\n근무지: %s (%s)",
                    name,
                    recIdx,
                    tasks != null ? String.join(", ", tasks) : "",
                    requirements != null ? String.join(", ", requirements) : "",
                    preferred != null ? String.join(", ", preferred) : "",
                    conditions != null ? String.join(", ", conditions) : "",
                    procedure != null ? String.join(", ", procedure) : "",
                    detailImages != null ? String.join(", ", detailImages) : "",
                    welfare,
                    workAddress,
                    subway
            );
        }
    }

    // URL에서 쿼리 파라미터 추출
    private static String extractQueryParam(String url, String key) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query == null) return null;
            for (String param : query.split("&")) {
                String[] parts = param.split("=", 2);
                if (parts.length == 2 && parts[0].equals(key)) {
                    return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // 공통 헤더 적용
    private static void applyCommonHeaders(HttpMessage req) {
        req.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        req.addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        req.addHeader("Accept-Encoding", "gzip, deflate, br");
        req.addHeader("Connection", "keep-alive");
    }

    // Ajax 요청용 헤더
    private static void applyAjaxHeaders(HttpPost req, String referer) {
        applyCommonHeaders(req);
        req.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        req.addHeader("X-Requested-With", "XMLHttpRequest");
        req.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        req.addHeader("Referer", referer);
    }

    // 텍스트 정제
    private static String cleanText(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("\\s+", " ").trim();
    }

    // 섹션별 추출 (텍스트 + 이미지 대응, 여러 키워드 지원)
    private static List<String> extractSectionByKeywords(Document doc, String... keywords) {
        List<String> results = new ArrayList<>();
        boolean inSection = false;

        for (Element el : doc.select("p, b, span, li, img")) {
            String text;
            if (el.tagName().equals("img")) {
                String src = el.attr("src");
                if (!src.startsWith("http")) src = "https://www.saramin.co.kr" + src;
                text = "[이미지] " + src;
            } else {
                text = cleanText(el.text());
            }
            if (text.isEmpty()) continue;

            // 시작 키워드
            if (!inSection && Arrays.stream(keywords).anyMatch(text::contains)) {
                inSection = true;
                continue;
            }

            // 다른 섹션 키워드 만나면 종료
            if (inSection && (
                    text.contains("주요업무") || text.contains("담당업무") ||
                            text.contains("자격요건") || text.contains("지원자격") ||
                            text.contains("우대사항") || text.contains("우대조건") ||
                            text.contains("근무조건") || text.contains("고용형태") ||
                            text.contains("채용절차") || text.contains("전형절차"))) {
                break;
            }

            if (inSection) {
                results.add(text);
            }
        }
        return results;
    }

    /** Ajax로 공고 상세 크롤링 */
    public static JobDetail crawlJobDetailAjax(String jobName, String jobPageUrl) {
        String recIdx = extractQueryParam(jobPageUrl, "rec_idx");
        String searchUuid = extractQueryParam(jobPageUrl, "search_uuid");
        String searchword = extractQueryParam(jobPageUrl, "searchword");

        JobDetail info = new JobDetail();
        info.name = jobName;
        info.recIdx = recIdx;

        CookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {

            // 1. 세션 확보용 GET
            try {
                HttpGet init = new HttpGet(jobPageUrl);
                applyCommonHeaders(init);
                init.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                try (CloseableHttpResponse r1 = httpClient.execute(init)) {
                    if (r1.getEntity() != null) EntityUtils.consume(r1.getEntity());
                }
            } catch (Exception ignored) {}

            // 2. Ajax POST
            String ajaxUrl = "https://www.saramin.co.kr/zf_user/jobs/relay/view-ajax";
            HttpPost post = new HttpPost(ajaxUrl);
            applyAjaxHeaders(post, jobPageUrl);

            List<NameValuePair> params = new ArrayList<>(List.of(
                    new BasicNameValuePair("rec_idx", recIdx),
                    new BasicNameValuePair("rec_seq", "0"),
                    new BasicNameValuePair("utm_source", ""),
                    new BasicNameValuePair("utm_medium", ""),
                    new BasicNameValuePair("utm_term", ""),
                    new BasicNameValuePair("utm_campaign", ""),
                    new BasicNameValuePair("view_type", "search"),
                    new BasicNameValuePair("t_ref", "search"),
                    new BasicNameValuePair("t_ref_content", "generic"),
                    new BasicNameValuePair("t_ref_scnid", ""),
                    new BasicNameValuePair("search_uuid", searchUuid != null ? searchUuid : UUID.randomUUID().toString()),
                    new BasicNameValuePair("refer", ""),
                    new BasicNameValuePair("searchType", "search"),
                    new BasicNameValuePair("searchword", searchword != null ? searchword : ""),
                    new BasicNameValuePair("ref_dp", "SRI_050_VIEW_MTRX_RCT_NOINFO"),
                    new BasicNameValuePair("dpId", ""),
                    new BasicNameValuePair("recommendRecIdx", ""),
                    new BasicNameValuePair("referNonce", ""),
                    new BasicNameValuePair("trainingStudentCode", "")
            ));
            post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            try (CloseableHttpResponse resp = httpClient.execute(post)) {
                String body = EntityUtils.toString(resp.getEntity(), "UTF-8");
                Document doc = Jsoup.parseBodyFragment(body);

                // 본문 iframe 접근
                Element detailEl = doc.selectFirst("div.jv_cont.jv_detail iframe#iframe_content_0");
                if (detailEl != null) {
                    String iframeSrc = detailEl.attr("src");
                    if (!iframeSrc.startsWith("http")) {
                        iframeSrc = "https://www.saramin.co.kr" + iframeSrc;
                    }

                    HttpGet iframeReq = new HttpGet(iframeSrc);
                    applyCommonHeaders(iframeReq);
                    try (CloseableHttpResponse iframeResp = httpClient.execute(iframeReq)) {
                        String iframeHtml = EntityUtils.toString(iframeResp.getEntity(), StandardCharsets.UTF_8);
                        Document iframeDoc = Jsoup.parse(iframeHtml);

                        // 섹션별 추출 (키워드 변형 대응)
                        info.tasks       = extractSectionByKeywords(iframeDoc, "주요업무", "담당업무");
                        info.requirements = extractSectionByKeywords(iframeDoc, "자격요건", "지원자격");
                        info.preferred   = extractSectionByKeywords(iframeDoc, "우대사항", "우대조건");
                        info.conditions  = extractSectionByKeywords(iframeDoc, "근무조건", "고용형태");
                        info.procedure   = extractSectionByKeywords(iframeDoc, "채용절차", "전형절차");

                        // 이미지 (본문 전체 이미지)
                        info.detailImages = new ArrayList<>();
                        for (Element img : iframeDoc.select("img")) {
                            String src = img.attr("src");
                            if (!src.startsWith("http")) src = "https://www.saramin.co.kr" + src;
                            info.detailImages.add(src);
                        }
                    }
                }

                // 복리후생
                Map<String, String> welfareMap = new LinkedHashMap<>();
                Element welfareHeader = doc.selectFirst("h2:contains(복리후생)");
                if (welfareHeader != null) {
                    Element container = welfareHeader.nextElementSibling();
                    if (container != null) {
                        for (Element dl : container.select("dl")) {
                            Element k = dl.selectFirst("dt");
                            Element v = dl.selectFirst("dd");
                            if (k != null && v != null) welfareMap.put(k.text().trim(), v.text().trim());
                        }
                    }
                }
                info.welfare = welfareMap.entrySet().stream()
                        .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                        .collect(Collectors.joining(",", "{", "}"));

                // 근무지
                Element locHeader = doc.selectFirst("h2:contains(근무지위치)");
                if (locHeader != null) {
                    Element container = locHeader.nextElementSibling();
                    if (container != null) {
                        Element addressEl = container.selectFirst("span.spr_jview.txt_adr");
                        Element subwayEl = container.selectFirst("span.spr_jview.txt_subway");
                        info.workAddress = addressEl != null ? addressEl.text().trim() : "";
                        info.subway = subwayEl != null ? subwayEl.text().trim() : "";
                    }
                }

                System.out.println(jobName + " 상세 크롤링 완료");
            }

        } catch (Exception e) {
            System.err.println("AJAX 상세 실패: " + e.getMessage());
        }

        return info;
    }

    /** CSV 저장 */
    public static void saveAsCsv(List<JobDetail> jobs, Path path) throws IOException {
        List<String> header = List.of("공고명", "주요업무", "자격요건", "우대사항", "근무조건", "채용절차", "이미지", "복리후생(JSON)", "근무지 주소", "지하철");
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            pw.println(toCsvLine(header));
            for (JobDetail j : jobs) {
                pw.println(toCsvLine(Arrays.asList(j.toCsvRow())));
            }
        }
    }

    private static String toCsvLine(List<String> cols) {
        return cols.stream().map(JobCrawler::csvEscape).collect(Collectors.joining(","));
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String value = s.replace("\"", "\"\"");
        return needQuote ? "\"" + value + "\"" : value;
    }

    /** 실행 예시 */
    public static void main(String[] args) throws Exception {
        JobDetail info = crawlJobDetailAjax(
                "2025년 하반기 딥러닝/머신러닝/연구원/Python外 모집 공고",
                "https://www.saramin.co.kr/zf_user/jobs/relay/view?view_type=search&rec_idx=51909960&location=ts&searchword=python&searchType=recently&paid_fl=n&search_uuid=2ec5b985-6c30-48cb-bd4d-9dab1bcdf406"
        );

        List<JobDetail> jobs = List.of(info);
        saveAsCsv(jobs, Path.of("saramin_job.csv"));

        System.out.println(info);
    }
}
