public class Supplier {

    private int id;
    private String name;
    private String company;
    private String email;
    private String phone;
    private String address;

    public Supplier(int id, String name,
                    String company, String email,
                    String phone, String address) {

        this.id = id;
        this.name = name;
        this.company = company;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return name + " - " + company;
    }
}