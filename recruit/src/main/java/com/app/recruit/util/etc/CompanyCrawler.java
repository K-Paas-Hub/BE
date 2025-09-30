package com.app.recruit.util.etc;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyCrawler {

    public static class CompanyInfo {
        public String name;
        public String type;
        public String industry;
        public String website;
        public String address;
        public String introduce;

        public String[] toCsvRow() {
            return new String[]{
                    name != null ? name : "",
                    type != null ? type : "",
                    industry != null ? industry : "",
                    website != null ? website : "",
                    address != null ? address : "",
                    introduce != null ? introduce : ""
            };
        }

        @Override
        public String toString() {
            return String.format("[%s] %s / %s / %s / %s / %s",
                    name,
                    type != null ? type : "정보 없음",
                    industry,
                    website,
                    address,
                    introduce);
        }
    }

    public static CompanyInfo crawlCompanyInfo(String companyName, String link) {
        CompanyInfo info = new CompanyInfo();
        info.name = companyName;

        try (CloseableHttpClient httpClient = HttpClients.custom().build()) {
            HttpGet request = new HttpGet(link);
            request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.addHeader("Accept-Encoding", "gzip, deflate");
            request.addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 404) {
                    System.out.println(companyName + " 페이지가 존재하지 않습니다. 모든 정보는 null로 저장됩니다.");
                    return info;
                }

                String html = EntityUtils.toString(response.getEntity(), "UTF-8");
                Document doc = Jsoup.parse(html);

                // 기업 형태
                Element typeEl = doc.select(".area_company_infos .company_summary_tit").size() > 1
                        ? doc.select(".area_company_infos .company_summary_tit").get(1)
                        : null;
                info.type = (typeEl != null && !typeEl.text().trim().endsWith("명")) ? typeEl.text().trim() : null;

                // 업종
                Element industryDt = doc.selectFirst("dt:contains(업종)");
                info.industry = industryDt != null ? industryDt.nextElementSibling().text().trim() : "정보 없음";

                // 홈페이지
                Element websiteDt = doc.selectFirst("dt:contains(홈페이지)");
                Element websiteLink = websiteDt != null ? websiteDt.nextElementSibling().selectFirst("a") : null;
                info.website = websiteLink != null ? websiteLink.attr("href") : "정보 없음";

                // 주소
                Element addressDt = doc.selectFirst("dt:contains(주소)");
                Element addressP = addressDt != null ? addressDt.nextElementSibling().selectFirst("p.ellipsis") : null;
                info.address = addressP != null ? addressP.text().trim() : "정보 없음";

                // 기업 설명
                Element introduceEl = doc.selectFirst(".area_company_infos .company_introduce");
                info.introduce = introduceEl != null ? introduceEl.text().trim() : "정보 없음";

                System.out.println(companyName + " 정보 크롤링 완료");

            }
        } catch (Exception e) {
            System.err.println("페이지 요청 중 에러 발생: " + e.getMessage());
        }

        return info;
    }

    // CSV 저장 유틸
    public static void saveAsCsv(List<CompanyInfo> companies, Path path) throws IOException {
        List<String> header = List.of("회사명", "기업 형태", "업종", "홈페이지", "주소", "기업 설명");
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            pw.println(toCsvLine(header));
            for (CompanyInfo c : companies) {
                pw.println(toCsvLine(Arrays.asList(c.toCsvRow())));
            }
        }
    }

    private static String toCsvLine(List<String> cols) {
        return cols.stream().map(CompanyCrawler::csvEscape).collect(Collectors.joining(","));
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String value = s.replace("\"", "\"\"");
        return needQuote ? "\"" + value + "\"" : value;
    }

    // 실행 예시
    public static void main(String[] args) throws Exception {
        CompanyInfo info = crawlCompanyInfo("(주)이노플러스컴퍼니",
                "https://www.saramin.co.kr/zf_user/company-info/view?csn=RERNRGFMdytKOTBycyt4U1dnMUwyUT09");

        // 단일 회사 크롤링 후 CSV 저장
        List<CompanyInfo> companies = List.of(info);
        saveAsCsv(companies, Path.of("saramin_company.csv"));
    }
}
