package springboot.onlinebookstore.dto.book;

public record BookSearchParametersDto(String[] title, String[] author, String[] isbn,
                                      String[] price) {
}
