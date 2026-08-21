package org.example;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class Mesto1Test {

    String bearerToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2YTg4NDA3N2JlMmY4OWNiZTc5OTAzMzgiLCJpYXQiOjE3ODczMTQyOTUsImV4cCI6MTc4NzkxOTA5NX0.LYMw15XcnKmsOk1DjWY5B3d6CxymywHvtYmNn5chYXE";

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-mesto.education-services.ru";
    }

    @Test
    @DisplayName("Add a new photo")
    @Description("This test is for adding a new photo to Mesto.")
    void addNewPhoto() {
        given()
                .header("Content-type", "application/json")
                .auth().oauth2(bearerToken)
                .body("{\"name\":\"Москва\",\"link\":\"https://code.s3.yandex.net/qa-automation-engineer/java/files/paid-track/sprint1/photoSelenium.jpg\"}")
                .post("/api/cards")
                .then().statusCode(201);
    }

    @Test
    @DisplayName("Like the first photo")
    @Description("This test is for liking the first photo on Mesto.")
    public void likeTheFirstPhoto() {
        // Тест теперь не зависит от addNewPhoto — сам создаёт фото, которое будет лайкать.
        String photoId = createPhotoAndGetId();

        likePhotoById(photoId);
        deleteLikePhotoById(photoId);
    }

    @Step("Create a new photo and return its id")
    private String createPhotoAndGetId() {
        Response response = given()
                .header("Content-type", "application/json")
                .auth().oauth2(bearerToken)
                .body("{\"name\":\"Москва\",\"link\":\"https://code.s3.yandex.net/qa-automation-engineer/java/files/paid-track/sprint1/photoSelenium.jpg\"}")
                .post("/api/cards")
                .then().statusCode(201)
                .extract().response();

        // ВАЖНО: проверьте реальную структуру JSON-ответа при создании карточки.
        // Если сервер возвращает {"_id": "...", ...} напрямую — путь "_id" верный.
        // Если ответ обёрнут, например {"data": {"_id": "..."}} — используйте "data._id".
        // Раскомментируйте строку ниже один раз, чтобы посмотреть реальный ответ:
        // System.out.println(response.getBody().asString());

        String photoId = response.path("data._id");

        if (photoId == null) {
            throw new IllegalStateException(
                    "Не удалось извлечь _id созданного фото. Проверьте структуру ответа: "
                            + response.getBody().asString());
        }

        return photoId;
    }

    @Step("Like a photo by id")
    private void likePhotoById(String photoId) {
        given()
                .auth().oauth2(bearerToken)
                .put("/api/cards/{photoId}/likes", photoId)
                .then().assertThat().statusCode(200);
    }

    @Step("Delete like from the photo by id")
    private void deleteLikePhotoById(String photoId) {
        given()
                .auth().oauth2(bearerToken)
                .delete("/api/cards/{photoId}/likes", photoId)
                .then().assertThat().statusCode(200);
    }

}