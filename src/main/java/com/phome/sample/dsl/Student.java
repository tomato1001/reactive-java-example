package com.phome.sample.dsl;

/**
 * @author zw
 */
public class Student {

    private final String name;

    private Student(String name) {
        this.name = name;
    }

    static Student from(String name) {
        return new Student(name);
    }

    Student getUp() {
        logStep("getUp");
        return this;
    }

    Student breakfast() {
        logStep("breakfast");
        return this;
    }

    Student lunch() {
        logStep("lunch");
        return this;
    }

    Student dinner() {
        logStep("dinner");
        return this;
    }

    void goToBed() {
        logStep("goToBed");
    }


    void logStep(String op) {
        System.out.println(name + " => " + op);
    }

    public static void main(String[] args) {
        from("John")
                .getUp()
                .breakfast()
                .lunch()
                .dinner()
                .goToBed();
    }

}
