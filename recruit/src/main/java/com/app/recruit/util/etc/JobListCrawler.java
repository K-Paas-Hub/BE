package com.app.recruit.util.etc;

import com.app.recruit.entity.enums.DeadlineType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class JobListCrawler {

    private static final String BASE = "https://www.saramin.co.kr";
    private static final DateTimeFormatter POSTED_FMT = DateTimeFormatter.ofPattern("yy/MM/dd");
    private static final DateTimeFormatter DEADLINE_FMT = DateTimeFormatter.ofPattern("M/d");

    public static class JobPosting {
        public String company;
        public String companyInfo;
        public String title;
        public String postLink;
        public String location;
        public String career;
        public String education;
        public String employmentType;
        public String salaryRange;
        public LocalDate postedDate;
        public LocalDate deadline;
        public DeadlineType deadlineType;
        public String status;
        public String trendKeywords;
        public List<String> jobSectors;

        public String[] toCsvRow() {
            return new String[]{
                    company,
                    trendKeywords,
                    title,
                    postLink,
                    location,
                    career,
                    education,
                    employmentType,
                    deadline != null ? deadline.toString()
                            : (deadlineType != null ? deadlineType.getLabel() : ""),
                    salaryRange,
                    postedDate != null ? postedDate.toString() : "",
                    status,
                    jobSectors != null ? String.join(" ", jobSectors) : "",
                    companyInfo
            };
        }

        @Override
        public String toString() {
            return String.format("[%s] %s (%s)", company, title, postLink);
        }
    }

    public static List<JobPosting> crawlJobPosts(String keyword, int pages) throws Exception {
        List<JobPosting> jobs = new ArrayList<>();
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        try (CloseableHttpClient httpClient = HttpClients.custom().build()) {
            for (int page = 1; page <= pages; page++) {
                String url = BASE + "/zf_user/search/recruit?search_area=main&search_done=y&searchType=recently"
                        + "&search_optional_item=n"
                        + "&searchword=" + encoded
                        + "&recruitPage=" + page
                        + "&recruitSort=relation"
                        + "&recruitPageCount=40";

                HttpGet request = new HttpGet(url);
                request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                request.addHeader("Accept-Encoding", "gzip, deflate");
                request.addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String html = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Document doc = Jsoup.parse(html);

                    Elements jobListings = doc.select(".item_recruit");
                    for (Element job : jobListings) {
                        try {
                            JobPosting jp = new JobPosting();

                            Element corpA = job.selectFirst(".corp_name a");
                            if (corpA == null) continue;
                            jp.company = corpA.text().trim();
                            jp.companyInfo = BASE + corpA.attr("href");

                            Element titleA = job.selectFirst(".job_tit a");
                            if (titleA == null) continue;
                            jp.title = titleA.text().trim();
                            jp.postLink = BASE + titleA.attr("href");

                            Elements conds = job.select(".job_condition span");
                            jp.location = conds.size() > 0 ? conds.get(0).text().trim() : "";
                            jp.career = conds.size() > 1 ? conds.get(1).text().trim() : "";
                            jp.education = conds.size() > 2 ? conds.get(2).text().trim() : "";
                            jp.employmentType = conds.size() > 3 ? conds.get(3).text().trim() : "";
                            jp.salaryRange = conds.size() > 4 ? conds.get(4).text().trim() : "";

                            // 마감일
                            String rawDeadline = job.selectFirst(".job_date .date") != null ?
                                    job.selectFirst(".job_date .date").text().trim() : null;

                            if (rawDeadline != null) {
                                LocalDate now = LocalDate.now();

                                if (rawDeadline.contains("상시")) {
                                    jp.deadline = null;
                                    jp.deadlineType = DeadlineType.ALWAYS;

                                } else if (rawDeadline.contains("채용시")) {
                                    jp.deadline = null;
                                    jp.deadlineType = DeadlineType.UNTIL_FILLED;

                                } else if (rawDeadline.contains("오늘")) {
                                    jp.deadline = now;
                                    jp.deadlineType = DeadlineType.TODAY;

                                } else if (rawDeadline.contains("내일")) {
                                    jp.deadline = now.plusDays(1);
                                    jp.deadlineType = DeadlineType.TOMORROW;

                                } else if (rawDeadline.contains("/")) {
                                    try {
                                        String cleaned = rawDeadline.trim().replace("~", "").replaceAll("[^0-9/]", ""); // e.g. ~ 10/29
                                        MonthDay md = MonthDay.parse(cleaned, DEADLINE_FMT);
                                        LocalDate d = md.atYear(now.getYear());

                                        if (d.isBefore(now)) {
                                            d = d.plusYears(1);
                                        }

                                        jp.deadline = d;
                                        jp.deadlineType = DeadlineType.DATE;

                                    } catch (Exception e) {
                                        jp.deadline = null;
                                        jp.deadlineType = null; // 파싱 실패
                                    }
                                }
                            }

                            // 직무분야 + 등록/수정일
                            Element sectorEl = job.selectFirst(".job_sector");
                            if (sectorEl != null) {
                                String sectorText = sectorEl.text().trim();
                                String postedDateStr = null;
                                if (sectorText.contains("수정일")) {
                                    postedDateStr = sectorText.substring(sectorText.indexOf("수정일") + 3).trim();
                                    sectorText = sectorText.substring(0, sectorText.indexOf("수정일")).trim();
                                } else if (sectorText.contains("등록일")) {
                                    postedDateStr = sectorText.substring(sectorText.indexOf("등록일") + 3).trim();
                                    sectorText = sectorText.substring(0, sectorText.indexOf("등록일")).trim();
                                }
                                jp.jobSectors = Arrays.asList(sectorText.split("\\s+"));
                                if (postedDateStr != null && postedDateStr.length() >= 8) {
                                    try {
                                        jp.postedDate = LocalDate.parse(
                                                postedDateStr.replaceAll("[^0-9/]", "").substring(0, 8),
                                                POSTED_FMT
                                        );
                                    } catch (Exception ignore) {}
                                }
                            }

                            // 트렌드 키워드
                            Element badge = job.selectFirst(".area_badge .badge");
                            jp.trendKeywords = badge != null ? badge.text().trim() : "";

                            // 상태
                            if (jp.deadlineType == DeadlineType.ALWAYS || jp.deadlineType == DeadlineType.UNTIL_FILLED) {
                                jp.status = "open";
                            } else if (jp.deadline != null && jp.deadline.isBefore(LocalDate.now())) {
                                jp.status = "closed";
                            } else {
                                jp.status = "open";
                            }

                            jobs.add(jp);
                        } catch (Exception e) {
                            System.err.println("파싱 에러: " + e.getMessage());
                        }
                    }

                    System.out.println(page + " 페이지 크롤링 완료: " + jobListings.size() + "건");
                }

                Thread.sleep(1000); // 서버 부하 방지
            }
        }
        return jobs;
    }

    // CSV 저장 유틸 (옵션)
    public static void saveAsCsv(List<JobListCrawler.JobPosting> jobs, Path path) throws IOException {
        List<String> header = List.of(
                "회사명","트렌드_키워드","제목","공고 링크","지역","경력","학력","고용형태",
                "마감일","연봉정보","작성날짜","상태","직무분야","회사 정보"
        );
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            pw.println(toCsvLine(header));
            for (JobListCrawler.JobPosting j : jobs) {
                pw.println(toCsvLine(Arrays.asList(j.toCsvRow())));
            }
        }
    }

    private static String toCsvLine(List<String> cols) {
        return cols.stream().map(JobListCrawler::csvEscape).collect(Collectors.joining(","));
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String value = s.replace("\"", "\"\"");
        return needQuote ? "\"" + value + "\"" : value;
    }

    // 실행 예시
    public static void main(String[] args) throws Exception {
        List<JobListCrawler.JobPosting> jobs = crawlJobPosts("외국인 가능", 1);
        System.out.println("크롤링 결과: " + jobs.size() + "건");
        // CSV로 저장
        saveAsCsv(jobs, Path.of("saramin_foreign.csv"));
    }
}
