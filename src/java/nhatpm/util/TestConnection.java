package nhatpm.util;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection con = DBHelper.getConnection(); // Gọi hàm bạn đã có
            if (con != null) {
                System.out.println("✅ Kết nối thành công!");
                con.close();
            } else {
                System.out.println("❌ Kết nối thất bại!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Lỗi: Không tìm thấy driver JDBC.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Lỗi SQL khi kết nối:");
            e.printStackTrace();
        }
    }
}
