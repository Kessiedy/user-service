package com.userservice.dto;

import jakarta.validation.constraints.*;

public class UserCreateDto {

    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат Email")
    @Size(min = 5, max = 50, message = "Email должен содержать от 5 до 30 символов")
    private String email;

    @Min(value = 0, message = "Возраст не может быть отрицательным")
    @Max(value = 110, message = "Возраст не может быть больше 110")
    private Integer age;

    public UserCreateDto(){

    }

    public UserCreateDto(String name, String email, Integer age){
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "UserCreateDto{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                '}';
    }
}