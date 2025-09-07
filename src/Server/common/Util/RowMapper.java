package Server.common.Util;
import java.sql.ResultSet;
//Dùng để ánh xạ 1 dòng dữ liệu trong database sang 1 đối tượng java
public interface RowMapper<T> {
	T mapRow(ResultSet rs) throws Exception;
}