package springboot.onlinebookstore.queries;

public class SqlScriptPath {
    public static final String REMOVE_USER_DATA_SCRIPT =
            "database/users/remove-role-user-and-shopping-cart-from-table.sql";

    public static final String REMOVE_BOOK_DATA_SCRIPT =
            "database/books/remove-book-and-category-data-from-tables.sql";

    public static final String ADD_BOOK_SCRIPT =
            "database/books/add-books-to-book-table.sql";

    public static final String ADD_CATEGORY_SCRIPT =
            "database/books/add-categories-to-category-table.sql";

    public static final String ADD_CATEGORY_TO_BOOK_SCRIPT =
            "database/books/add-category-to-book-in-book-category-table.sql";

    public static final String ADD_ROLE_SCRIPT =
            "database/users/add-roles-to-role-table.sql";

    public static final String ADD_USER_SCRIPT =
            "database/users/add-users-to-user-table.sql";

    public static final String ADD_USERS_ROLE_SCRIPT =
            "database/users/add-users-roles-to-user-role-table.sql";

    public static final String ADD_SHOPPING_CART_SCRIPT =
            "database/users/add-shopping-cart-to-shopping-cart-table.sql";

    public static final String ADD_CART_ITEM_SCRIPT =
            "database/users/add-cart-item-to-cart-item-table.sql";

    public static final String ADD_ORDER_SCRIPT =
            "database/users/add-order-to-order-table.sql";
    public static final String ADD_ORDER_ITEM_SCRIPT =
            "database/users/add-order-item-to-order-items-table.sql";
}
