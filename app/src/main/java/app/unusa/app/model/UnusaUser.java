package app.unusa.app.model;

import android.graphics.drawable.Drawable;

import com.orm.SugarRecord;

public class UnusaUser extends SugarRecord {

    public int image;
    public String firstName = "";
    public String lastName = "";
    public String
            email = "",
            userId = "",
            profilePhoto = "",
            profilePhotoMother = "",
            profilePhotoFather = "",
            username = "",
            password = "",
            company = "",
            gender = "",
            address = "",
            regDate = "",
            lastSeen = "",
            userType = "regular",
            verified = "0",
            reg_num = "",
            company_position = "",
            phoneNumber = "",
            next_mother_name = "",
            next_father_name = "",
            next_mother_phone = "",
            next_father_phone = "",
            next_address = "";
    public String nationality = "";


    public UnusaUser(String userId, String nationality, String userType, int image, Drawable imageDrw, String firstName, String lastName, String email, String profilePhoto, String username, String password, String company, String gender, String address, String regDate, String lastSeen, String phoneNumber, String reg_num,
                     String company_position
    ) {
        this.image = image;
        this.company_position = company_position;
        this.reg_num = reg_num;
        this.nationality = nationality;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profilePhoto = profilePhoto;
        this.username = username;
        this.password = password;
        this.company = company;
        this.gender = gender;
        this.address = address;
        this.regDate = regDate;
        this.lastSeen = lastSeen;
        this.phoneNumber = phoneNumber;
        this.userId = userId;
    }

    public UnusaUser() {
    }


}
