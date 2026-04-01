package service;

import dao.impl.ServiceDao;
import model.Service;

import java.sql.SQLException;
import java.util.List;

public class ServiceMgmtService {
    private final ServiceDao dao = new ServiceDao();

    public String addService(String name, double price, String unit) throws SQLException {
        if (name.isBlank())  return "Tên dịch vụ không được để trống.";
        if (price < 0)       return "Đơn giá không được âm.";
        if (dao.existsName(name)) return "Dịch vụ '" + name + "' đã tồn tại.";
        dao.insert(new Service(name, price, unit));
        return null;
    }

    public String updateService(int id, String name, double price, String unit) throws SQLException {
        Service sv = dao.findById(id);
        if (sv == null) return "Không tìm thấy dịch vụ ID = " + id;
        if (name.isBlank()) return "Tên dịch vụ không được để trống.";
        if (price < 0)      return "Đơn giá không được âm.";
        if (dao.existsNameExcludeId(name, id)) return "Dịch vụ '" + name + "' đã tồn tại.";

        sv.setName(name);
        sv.setUnitPrice(price);
        sv.setUnit(unit);
        dao.update(sv);
        return null;
    }

    public String deleteService(int id) throws SQLException {
        if (dao.findById(id) == null) return "Không tìm thấy dịch vụ ID = " + id;
        dao.delete(id);
        return null;
    }

    public Service findById(int id) throws SQLException { return dao.findById(id); }
    public List<Service> getAll()   throws SQLException { return dao.findAll(); }
}
