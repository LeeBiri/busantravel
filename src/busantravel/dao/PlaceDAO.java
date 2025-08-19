package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.model.Restaurant;
import busantravel.model.Tourist;
import busantravel.ui.PlaceDetailInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PlaceDAO {
    private DBConnectionMgr pool;

    public PlaceDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    private String removeHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "");
    }

    public List<Object> getPlacesByCategory(String category) {
        return getPlacesByCategory(category, 1, Integer.MAX_VALUE); // 기존 메서드는 모든 결과를 가져오도록 위임
    }

    public List<Object> getPlacesByCategory(String category, int page, int pageSize) {
        if (!DBConnectionMgr.isDbConnected()) {
            return getDummyPlaces(category);
        }
        List<Object> places = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String tableName = getTableName(category);
        String sql = "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?";
        int offset = (page - 1) * pageSize;

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                switch (tableName) {
                    case "tourist":
                        Tourist tourist = new Tourist(
                                rs.getString("t_id"),
                                rs.getString("t_place"),
                                removeHtmlTags(rs.getString("t_intro")),
                                rs.getString("t_num"),
                                rs.getString("t_tra"),
                                rs.getString("t_infor"),
                                rs.getString("t_addr"),
                                rs.getString("t_thum")
                        );
                        places.add(tourist);
                        break;
                    case "restaurant":
                        Restaurant restaurant = new Restaurant(
                                rs.getString("r_id"),
                                rs.getString("r_place"),
                                removeHtmlTags(rs.getString("r_intro")),
                                rs.getString("r_num"),
                                rs.getString("r_infor"),
                                rs.getString("r_addr"),
                                rs.getString("r_thum"),
                                null
                        );
                        places.add(restaurant);
                        break;
                    case "stay":
                        Lodging lodging = new Lodging(
                                rs.getString("l_id"),
                                rs.getString("l_place"),
                                removeHtmlTags(rs.getString("l_intro")),
                                rs.getString("l_num"),
                                rs.getString("l_tra"),
                                rs.getString("l_infor"),
                                rs.getString("l_addr"),
                                rs.getString("l_thum")
                        );
                        places.add(lodging);
                        break;
                    case "festival":
                        Festival festival = new Festival(
                                rs.getString("f_id"),
                                rs.getString("f_name"),
                                removeHtmlTags(rs.getString("f_intro")),
                                rs.getString("f_plan"),
                                rs.getString("f_tra"),
                                rs.getString("f_infor"),
                                rs.getString("f_addr"),
                                rs.getString("f_thum")
                        );
                        places.add(festival);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return places;
    }

    public int getTotalPlaceCount(String category) {
        if (!DBConnectionMgr.isDbConnected()) {
            return getDummyPlaces(category).size();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int totalCount = 0;

        String tableName = getTableName(category);
        String sql = "SELECT COUNT(*) FROM " + tableName;

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return totalCount;
    }

    public List<Object> searchPlaces(String query, String category) {
        return searchPlaces(query, category, 1, Integer.MAX_VALUE);
    }

    public List<Object> searchPlaces(String query, String category, int page, int pageSize) {
        if (!DBConnectionMgr.isDbConnected()) {
            List<Object> dummyPlaces = new ArrayList<>();
            dummyPlaces.addAll(getDummyPlaces(category));
            return dummyPlaces;
        }
        List<Object> places = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String tableName = getTableName(category);
        String placeColumnName = getPlaceColumnName(category);

        String sql = "SELECT * FROM " + tableName + " WHERE " + placeColumnName + " LIKE ? LIMIT ? OFFSET ?";
        int offset = (page - 1) * pageSize;

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            String likeQuery = "%" + query + "%";
            pstmt.setString(1, likeQuery);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                switch (tableName) {
                    case "tourist":
                        places.add(new Tourist(rs.getString("t_id"), rs.getString("t_place"), removeHtmlTags(rs.getString("t_intro")), rs.getString("t_num"), rs.getString("t_tra"), rs.getString("t_infor"), rs.getString("t_addr"), rs.getString("t_thum")));
                        break;
                    case "restaurant":
                        places.add(new Restaurant(rs.getString("r_id"), rs.getString("r_place"), removeHtmlTags(rs.getString("r_intro")), rs.getString("r_num"), rs.getString("r_infor"), rs.getString("r_addr"), rs.getString("r_thum"), rs.getString("r_menu")));
                        break;
                    case "stay":
                        places.add(new Lodging(rs.getString("l_id"), rs.getString("l_place"), removeHtmlTags(rs.getString("l_intro")), rs.getString("l_num"), rs.getString("l_tra"), rs.getString("l_infor"), rs.getString("l_addr"), rs.getString("l_thum")));
                        break;
                    case "festival":
                        places.add(new Festival(rs.getString("f_id"), rs.getString("f_name"), removeHtmlTags(rs.getString("f_intro")), rs.getString("f_plan"), rs.getString("f_tra"), rs.getString("f_infor"), rs.getString("f_addr"), rs.getString("f_thum")));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return places;
    }

    public int getTotalSearchCount(String query, String category) {
        if (!DBConnectionMgr.isDbConnected()) {
            return getDummyPlaces(category).size();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int totalCount = 0;

        String tableName = getTableName(category);
        String placeColumnName = getPlaceColumnName(category);

        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + placeColumnName + " LIKE ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            String likeQuery = "%" + query + "%";
            pstmt.setString(1, likeQuery);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return totalCount;
    }

    private String getTableName(String category) {
        switch (category) {
            case "관광지":
                return "tourist";
            case "맛집":
                return "restaurant";
            case "숙소":
                return "stay";
            case "축제":
                return "festival";
            default:
                return "tourist";
        }
    }

    public PlaceDetailInfo getPlaceDetail(String placeName, String category) {
        if (!DBConnectionMgr.isDbConnected()) {
            return getDummyPlaceDetail(placeName, category);
        }
        PlaceDetailInfo placeDetailInfo = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String tableName = getTableName(category);
        String sql = "SELECT * FROM " + tableName + " WHERE " + getPlaceColumnName(category) + " = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, placeName);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                if ("tourist".equals(tableName)) {
                    placeDetailInfo = new PlaceDetailInfo(
                            rs.getString("t_place"),
                            removeHtmlTags(rs.getString("t_intro")),
                            rs.getString("t_thum"),
                            rs.getString("t_tra"),
                            rs.getString("t_infor"),
                            rs.getString("t_addr")
                    );
                } else if ("restaurant".equals(tableName)) {
                    placeDetailInfo = new PlaceDetailInfo(
                            rs.getString("r_place"),
                            removeHtmlTags(rs.getString("r_intro")),
                            rs.getString("r_thum"),
                            "", // transportation for restaurant
                            rs.getString("r_infor"),
                            rs.getString("r_addr"),
                            null,
                            rs.getString("r_num")
                    );
                } else if ("stay".equals(tableName)) {
                    placeDetailInfo = new PlaceDetailInfo(
                            rs.getString("l_place"),
                            rs.getString("l_intro"),
                            rs.getString("l_thum"),
                            rs.getString("l_tra"),
                            rs.getString("l_infor"),
                            rs.getString("l_addr")
                    );
                } else if ("festival".equals(tableName)) {
                    placeDetailInfo = new PlaceDetailInfo(
                            rs.getString("f_name"),
                            removeHtmlTags(rs.getString("f_intro")),
                            rs.getString("f_thum"), // festival 썸네일 컬럼을 f_thum으로 변경
                            rs.getString("f_tra"),
                            rs.getString("f_plan"), // f_infor -> f_plan 으로 변경
                            rs.getString("f_addr")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return placeDetailInfo;
    }

    private String getPlaceColumnName(String category) {
        switch (category) {
            case "관광지":
                return "t_place";
            case "맛집":
                return "r_place";
            case "숙소":
                return "l_place";
            case "축제":
                return "f_name";
            default:
                return "t_place";
        }
    }

    private String getIntroColumnName(String category) {
        switch (category) {
            case "관광지":
                return "t_intro";
            case "맛집":
                return "r_intro";
            case "숙소":
                return "l_intro";
            case "축제":
                return "f_intro";
            default:
                return "t_intro";
        }
    }

    private String getThumColumnName(String category) {
        switch (category) {
            case "관광지":
                return "t_thum";
            case "맛집":
                return "r_thum";
            case "숙소":
                return "l_thum";
            case "축제":
                return "t_thum"; // 축제 썸네일 컬럼을 t_thum으로 변경
            default:
                return "t_thum";
        }
    }

    public Object getPlaceByTypeAndId(String type, String id) {
    if (!DBConnectionMgr.isDbConnected()) {
        // 더미 데이터에서 검색
        for (Object o : getDummyPlaces(type)) {
            if ((type.equals("관광지") && ((Tourist) o).getT_id().equals(id)) ||
                (type.equals("맛집") && ((Restaurant) o).getR_id().equals(id)) ||
                (type.equals("숙소") && ((Lodging) o).getL_id().equals(id)) ||
                (type.equals("축제") && ((Festival) o).getF_id().equals(id))) {
                return o;
            }
        }
        return null;
    }

    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Object result = null;
    String table = getTableName(type);
    String sql = "SELECT * FROM " + table + " WHERE ";

    switch (type) {
        case "관광지":
            sql += "t_id = ?";
            break;
        case "맛집":
            sql += "r_id = ?";
            break;
        case "숙소":
            sql += "l_id = ?";
            break;
        case "축제":
            sql += "f_id = ?";
            break;
        default:
            return null;
    }

    try {
        con = pool.getConnection();
        pstmt = con.prepareStatement(sql);
        pstmt.setString(1, id);
        rs = pstmt.executeQuery();

        if (rs.next()) {
            switch (type) {
                case "관광지":
                    result = new Tourist(
                            rs.getString("t_id"),
                            rs.getString("t_place"),
                            removeHtmlTags(rs.getString("t_intro")),
                            rs.getString("t_num"),
                            rs.getString("t_tra"),
                            rs.getString("t_infor"),
                            rs.getString("t_addr"),
                            rs.getString("t_thum"));
                    break;
                case "맛집":
                    result = new Restaurant(
                            rs.getString("r_id"),
                            rs.getString("r_place"),
                            removeHtmlTags(rs.getString("r_intro")),
                            rs.getString("r_num"),
                            rs.getString("r_infor"),
                            rs.getString("r_addr"),
                            rs.getString("r_thum"),
                            null);
                    break;
                case "숙소":
                    result = new Lodging(
                            rs.getString("l_id"),
                            rs.getString("l_place"),
                            removeHtmlTags(rs.getString("l_intro")),
                            rs.getString("l_num"),
                            rs.getString("l_tra"),
                            rs.getString("l_infor"),
                            rs.getString("l_addr"),
                            rs.getString("l_thum"));
                    break;
                case "축제":
                    result = new Festival(
                            rs.getString("f_id"),
                            rs.getString("f_name"),
                            removeHtmlTags(rs.getString("f_intro")),
                            rs.getString("f_plan"),
                            rs.getString("f_tra"),
                            rs.getString("f_infor"),
                            rs.getString("f_addr"),
                            rs.getString("f_thum"));
                    break;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        pool.freeConnection(con, pstmt, rs);
    }

    return result;
}


    // --- 더미 데이터 생성 메서드 ---

    private List<Object> getDummyPlaces(String category) {
        List<Object> dummyPlaces = new ArrayList<>();
        switch (category) {
            case "관광지":
                dummyPlaces.add(new Tourist("D001", "해운대해수욕장 (더미)", "설명이 없습니다.", "051-749-4335", "정보 없음", "연중무휴", "부산 해운대구", "https://www.visitbusan.net/upload/preview/20240408133523345.jpg"));
                dummyPlaces.add(new Tourist("D002", "광안리해수욕장 (더미)", "설명이 없습니다.", "051-622-4251", "정보 없음", "연중무휴", "부산 수영구", "https://www.visitbusan.net/upload/preview/20231127134103039.jpg"));
                break;
            case "맛집":
                dummyPlaces.add(new Restaurant("D003", "돼지국밥집 (더미)", "설명이 없습니다.", "정보 없음", "매일 09:00 - 21:00", "부산 어딘가", "https://www.visitbusan.net/upload/preview/20231127135349987.jpg", "돼지국밥 9,000원"));
                break;
            case "숙소":
                dummyPlaces.add(new Lodging("D004", "부산호텔 (더미)", "설명이 없습니다.", "051-123-4567", "정보 없음", "정보 없음", "부산 동구", "https://www.visitbusan.net/upload/preview/20231127140429858.jpg"));
                break;
            case "축제":
                dummyPlaces.add(new Festival("D005", "부산불꽃축제 (더미)", "설명이 없습니다.", "매년 11월", "정보 없음", "정보 없음", "부산 광안리", "https://www.visitbusan.net/upload/preview/20231127140012999.jpg"));
                break;
        }
        return dummyPlaces;
    }

    private PlaceDetailInfo getDummyPlaceDetail(String placeName, String category) {
        switch (placeName) {
            case "해운대해수욕장 (더미)":
                return new PlaceDetailInfo("해운대해수욕장 (더미)", "부산의 대표적인 해수욕장입니다. (더미 데이터)", "https://www.visitbusan.net/upload/preview/20240408133523345.jpg", "도시철도 2호선 해운대역", "연중무휴", "부산광역시 해운대구 우동");
            case "돼지국밥집 (더미)":
                return new PlaceDetailInfo("돼지국밥집 (더미)", "부산의 소울푸드 돼지국밥을 맛볼 수 있습니다. (더미 데이터)", "https://www.visitbusan.net/upload/preview/20231127135349987.jpg", "도시철도 1호선 서면역", "매일 09:00 - 21:00", "부산광역시 부산진구", "돼지국밥: 9,000원\n순대국밥: 9,000원", "051-999-9999");
            default:
                return new PlaceDetailInfo(placeName, "세부 정보를 찾을 수 없습니다. (더미 데이터)", "", "정보 없음", "정보 없음", "정보 없음");
        }
    }
}
