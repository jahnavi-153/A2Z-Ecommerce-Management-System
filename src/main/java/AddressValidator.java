public class AddressValidator {

    public static boolean validPhone(String phone) {

        return phone != null
                && phone.matches("[6-9][0-9]{9}");
    }

    public static boolean validHouse(String house) {

        return house != null
                && !house.isBlank()
                && house.length() <= 50
                && house.matches(".*[\\p{L}0-9].*");
    }

    public static boolean validStreet(String street) {

        return street != null
                && street.trim().length() >= 3
                && street.length() <= 100
                && street.matches(".*\\p{L}.*");
    }

    public static boolean validCity(String city) {

        return city != null
                && city.length() >= 2
                && city.length() <= 60
                && city.matches("[\\p{L}\\p{M} .'-]+")
                && city.matches(".*\\p{L}.*");
    }

    public static boolean validPin(String pin) {

        return pin != null
                && pin.matches("[1-9][0-9]{5}");
    }

    public static boolean validState(String state) {

        return state != null && !state.isBlank();
    }

    public static String validate(
            String phone,
            String house,
            String street,
            String landmark,
            String city,
            String pin,
            String state) {

        if (!validPhone(phone)) {

            return "Enter a valid 10-digit mobile number.";
        }

        if (!validHouse(house)) {

            return "Enter a valid house or flat number.";
        }

        if (!validStreet(street)) {

            return "Enter a valid street or area.";
        }

        if (landmark.length() > 80) {

            return "Landmark must not exceed 80 characters.";
        }

        if (!validCity(city)) {

            return "Enter a valid city name.";
        }

        if (!validPin(pin)) {

            return "Enter a valid 6-digit PIN code.";
        }

        if (!validState(state)) {

            return "Please select your state.";
        }

        return null;
    }

    public static String buildAddress(
            String phone,
            String house,
            String street,
            String landmark,
            String city,
            String pin,
            String state) {

        String address = house + ", " + street;

        if (!landmark.isBlank()) {

            address += ", Near " + landmark;
        }

        address += ", " + city
                + ", " + state
                + " - " + pin
                + ", India"
                + "\nPhone: " + phone;

        return address;
    }
}