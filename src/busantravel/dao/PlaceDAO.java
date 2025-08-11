package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.ui.PlaceDetailInfo;
import busantravel.model.Restaurant;
import busantravel.model.Tourist;

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
        List<Object> places = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String tableName = getTableName(category);
        String sql = "SELECT * FROM " + tableName;

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
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
                                rs.getString("t_rest"),
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
                                rs.getString("r_rest"),
                                rs.getString("r_addr"),
                                rs.getString("r_thum")
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
                                rs.getString("l_addr")
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
                                rs.getString("f_plan"), // f_rest -> f_plan
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

    public List<Object> searchPlaces(String query) {
        List<Object> places = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String[] categories = {"관광지", "맛집", "숙소", "축제"};

        try {
            con = pool.getConnection();
            for (String category : categories) {
                String tableName = getTableName(category);
                String placeColumnName = getPlaceColumnName(category);
                String sql = "SELECT * FROM " + tableName + " WHERE " + placeColumnName + " LIKE ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, "%" + query + "%");
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    if ("tourist".equals(tableName)) {
                        Tourist tourist = new Tourist(
                                rs.getString("t_id"),
                                rs.getString("t_place"),
                                removeHtmlTags(rs.getString("t_intro")),
                                rs.getString("t_num"),
                                rs.getString("t_tra"),
                                rs.getString("t_rest"),
                                rs.getString("t_addr"),
                                rs.getString("t_thum")
                        );
                        places.add(tourist);
                    } else if ("restaurant".equals(tableName)) {
                        Restaurant restaurant = new Restaurant(
                                rs.getString("r_id"),
                                rs.getString("r_place"),
                                removeHtmlTags(rs.getString("r_intro")),
                                rs.getString("r_num"),
                                rs.getString("r_rest"),
                                rs.getString("r_addr"),
                                rs.getString("r_thum")
                        );
                        places.add(restaurant);
                    } else if ("stay".equals(tableName)) {
                        Lodging lodging = new Lodging(
                                rs.getString("l_id"),
                                rs.getString("l_place"),
                                removeHtmlTags(rs.getString("l_intro")),
                                rs.getString("l_num"),
                                rs.getString("l_tra"),
                                rs.getString("l_infor"),
                                rs.getString("l_addr")
                        );
                        places.add(lodging);
                    } else if ("festival".equals(tableName)) {
                        Festival festival = new Festival(
                                rs.getString("f_id"),
                                rs.getString("f_name"),
                                removeHtmlTags(rs.getString("f_intro")),
                                rs.getString("f_plan"),
                                rs.getString("f_tra"),
                                rs.getString("f_plan"), // f_rest -> f_plan
                                rs.getString("f_addr"),
                                rs.getString("f_thum")
                        );
                        places.add(festival);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return places;
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
                            rs.getString("t_rest"),
                            rs.getString("t_addr")
                    );
                } else if ("restaurant".equals(tableName)) {
                    placeDetailInfo = new PlaceDetailInfo(
                            rs.getString("r_place"),
                            removeHtmlTags(rs.getString("r_intro")),
                            rs.getString("r_thum"),
                            "", // transportation for restaurant
                            rs.getString("r_rest"),
                            rs.getString("r_addr")
                    );
                } else if ("stay".equals(tableName)) {
                    placeDetailInfo = new PlaceDetailInfo(
                            rs.getString("l_place"),
                            removeHtmlTags(rs.getString("l_intro")),
                            "", // thumbnail for lodging
                            rs.getString("l_tra"),
                            rs.getString("l_infor"), // restDay for lodging is l_infor
                            rs.getString("l_addr")
                    );
                } else if ("festival".equals(tableName)) {
                    placeDetailInfo = new PlaceDetailInfo(
                            rs.getString("f_name"),
                            removeHtmlTags(rs.getString("f_intro")),
                            rs.getString("f_thum"), // festival 썸네일 컬럼을 f_thum으로 변경
                            rs.getString("f_tra"),
                            removeHtmlTags(rs.getString("f_intro")), // restDay for festival is f_intro
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
                return null;
            case "축제":
                return "t_thum"; // 축제 썸네일 컬럼을 t_thum으로 변경
            default:
                return "t_thum";
        }
    }
}
