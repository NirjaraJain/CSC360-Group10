package com.library.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain entity representing a Library Patron/Member.
 */
public class Member implements BaseEntity<String> {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String membershipType; // e.g., "Student", "Faculty", "Regular"
    private LocalDate joinDate;
    private int activeLoansCount;
    private String status; // e.g., "ACTIVE", "SUSPENDED"

    public Member() {
    }

    public Member(String id, String name, String email, String phone, String membershipType, LocalDate joinDate, int activeLoansCount, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.membershipType = membershipType;
        this.joinDate = joinDate;
        this.activeLoansCount = activeLoansCount;
        this.status = status;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public int getActiveLoansCount() {
        return activeLoansCount;
    }

    public void setActiveLoansCount(int activeLoansCount) {
        this.activeLoansCount = activeLoansCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
