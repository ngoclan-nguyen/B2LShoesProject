package com.example.model;

import java.io.Serializable;

public class Consignee implements Serializable {

    private static final long serialVersionUID = 1L; // Cần thiết cho Serializable

    private String fullName;
    private String phone;
    private String email;
    private String address; // Địa chỉ chi tiết (số nhà, tên đường)
    private String province; // Tỉnh/Thành phố
    private String district; // Quận/Huyện
    private String ward;     // Phường/Xã
    private String notes;    // Ghi chú của khách hàng (nếu có)

    public Consignee() {
    }

    public Consignee(String fullName, String phone, String email, String address, String province, String district, String ward, String notes) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.notes = notes;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Consignee{" +
                "fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", province='" + province + '\'' +
                ", district='" + district + '\'' +
                ", ward='" + ward + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}