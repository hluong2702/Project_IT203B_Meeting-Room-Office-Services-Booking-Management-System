package service;

import dao.impl.EquipmentDao;
import model.Equipment;

import java.sql.SQLException;
import java.util.List;

public class EquipmentService {
    private final EquipmentDao dao = new EquipmentDao();

    public String addEquipment(String name, int totalQty) throws SQLException {
        if (name.isBlank())   return "Tên thiết bị không được để trống.";
        if (totalQty <= 0)    return "Số lượng phải lớn hơn 0.";
        if (dao.existsName(name)) return "Thiết bị '" + name + "' đã tồn tại.";

        dao.insert(new Equipment(name, totalQty));
        return null;
    }

    public String updateEquipment(int id, String name, int total, int available,
                                  String status) throws SQLException {
        Equipment eq = dao.findById(id);
        if (eq == null) return "Không tìm thấy thiết bị ID = " + id;
        if (name.isBlank())   return "Tên thiết bị không được để trống.";
        if (total <= 0)       return "Tổng số lượng phải lớn hơn 0.";
        if (available < 0 || available > total)
            return "Số lượng khả dụng không hợp lệ (0 ≤ khả dụng ≤ tổng).";
        if (dao.existsNameExcludeId(name, id)) return "Tên thiết bị '" + name + "' đã tồn tại.";

        eq.setName(name);
        eq.setTotalQuantity(total);
        eq.setAvailableQty(available);
        eq.setStatus(status);
        dao.update(eq);
        return null;
    }

    public String deleteEquipment(int id) throws SQLException {
        if (dao.findById(id) == null) return "Không tìm thấy thiết bị ID = " + id;
        if (dao.isUsedInBooking(id))
            return "Không thể xóa thiết bị đang được sử dụng trong booking.";
        dao.delete(id);
        return null;
    }

    public Equipment findById(int id) throws SQLException { return dao.findById(id); }
    public List<Equipment> getAll()    throws SQLException { return dao.findAll(); }
    public List<Equipment> getActive() throws SQLException { return dao.findActive(); }
}
