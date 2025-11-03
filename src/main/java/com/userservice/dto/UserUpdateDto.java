package com.userservice.dto;

import jakarta.validation.constraints.*;

public class UserUpdateDto {

    @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов")
    private String name;

    @Email(message = "Некорректный формат Email")
    @Size(min = 5, max = 50, message = "Email должен содержать от 5 до 30 символов")
    private String email;

    @Min(value = 0, message = "Возраст не может быть отрицательным")
    @Max(value = 110, message = "Возраст не может быть больше 110")
    private Integer age;

    public UserUpdateDto(){
    }

    public UserUpdateDto(String name, String email, Integer age){
        this.name = name;
        this.email = email;
        this.age = age;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}