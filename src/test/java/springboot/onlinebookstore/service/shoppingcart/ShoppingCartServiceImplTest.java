package springboot.onlinebookstore.service.shoppingcart;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import springboot.onlinebookstore.dto.cartitem.request.CartItemQuantityRequestDto;
import springboot.onlinebookstore.dto.cartitem.request.CartItemRequestDto;
import springboot.onlinebookstore.dto.cartitem.response.CartItemResponseDto;
import springboot.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.mapper.CartItemMapper;
import springboot.onlinebookstore.mapper.ShoppingCartMapper;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.model.CartItem;
import springboot.onlinebookstore.model.Category;
import springboot.onlinebookstore.model.ShoppingCart;
import springboot.onlinebookstore.model.User;
import springboot.onlinebookstore.repository.book.BookRepository;
import springboot.onlinebookstore.repository.cartitem.CartItemRepository;
import springboot.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import springboot.onlinebookstore.repository.user.UserRepository;
import springboot.onlinebookstore.service.impl.ShoppingCartServiceImpl;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    private static final Long USER_ID = 1L;
    private static final Integer QUANTITY = 1;
    private static final Integer CHANGED_QUANTITY = 2;
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;

    @Test
    @DisplayName("Get shopping cart by existing user")
    void getCartByUser_ExistingUser_ReturnsValidShoppingCart() {
        User user = new User(USER_ID);
        ShoppingCartResponseDto shoppingCartResponseDto = getShoppingCartResponseDto();
        ShoppingCart shoppingCart = getShoppingCart(user);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(shoppingCartResponseDto);
        ShoppingCartResponseDto actual = shoppingCartService.getCartByUser(USER_ID);
        Assertions.assertEquals(shoppingCartResponseDto, actual);
        verify(shoppingCartRepository, times(2)).findByUserId(anyLong());
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verifyNoMoreInteractions(shoppingCartRepository);
        verifyNoInteractions(userRepository);
        verifyNoMoreInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("Creating and getting shopping cart by valid user")
    void getCartByUser_ValidUser_CreateAndReturnsShoppingCart() {
        User user = new User(USER_ID);
        ShoppingCartResponseDto shoppingCartResponseDto = getShoppingCartResponseDto();
        ShoppingCart shoppingCart = getShoppingCart(user);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(shoppingCartRepository.save(any())).thenReturn(shoppingCart);
        when(shoppingCartMapper.toDto(any())).thenReturn(shoppingCartResponseDto);
        ShoppingCartResponseDto actual = shoppingCartService.getCartByUser(USER_ID);
        Assertions.assertEquals(shoppingCartResponseDto, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verify(userRepository, times(1)).getReferenceById(USER_ID);
        verify(shoppingCartRepository, times(1)).save(any());
        verify(shoppingCartMapper, times(1)).toDto(any());
        verifyNoMoreInteractions(shoppingCartRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("Add new item to shopping cart valid item and valid cart")
    void addItemToCart_ValidCartAndValidItem_ReturnsCartDto() {
        User user = new User(USER_ID);
        ShoppingCart shoppingCart = getShoppingCart(user);
        CartItemRequestDto cartItemRequestDto =
                new CartItemRequestDto(getBookEntity().getId(), QUANTITY);
        CartItem cartItem = getCartItem(shoppingCart);
        CartItemResponseDto cartItemResponseDto = new CartItemResponseDto(cartItem.getId(),
                getBookEntity().getId(), getBookEntity().getTitle(), cartItem.getQuantity());
        ShoppingCartResponseDto shoppingCartResponseDto = getShoppingCartResponseDto();
        shoppingCartResponseDto.setCartItems(Set.of(cartItemResponseDto));
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemMapper.toEntity(cartItemRequestDto)).thenReturn(cartItem);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(getBookEntity()));
        when(cartItemRepository.save(any())).thenReturn(cartItem);
        when(shoppingCartMapper.toDto(any())).thenReturn(shoppingCartResponseDto);
        ShoppingCartResponseDto actual = shoppingCartService
                .addItemToCart(USER_ID, cartItemRequestDto);
        Assertions.assertEquals(shoppingCartResponseDto, actual);
    }

    @Test
    @DisplayName("""
            Add new item to shopping cart, cart is not found, returns EntityNotFoundException
            """)
    void addItemToCart_CartNotFound_ReturnsException() {
        CartItemRequestDto cartItemRequestDto =
                new CartItemRequestDto(getBookEntity().getId(), QUANTITY);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.addItemToCart(USER_ID, cartItemRequestDto)
        );
        String expected = "Can't find shopping cart for user with id: " + USER_ID;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verifyNoMoreInteractions(shoppingCartRepository);
        verifyNoInteractions(cartItemMapper);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(cartItemRepository);
        verifyNoInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("Add existing item to shopping cart, returns exception")
    void addItemToCart_ItemAlreadyExists_ReturnsException() {
        User user = new User(USER_ID);
        ShoppingCart shoppingCart = getShoppingCart(user);
        CartItem cartItem = getCartItem(shoppingCart);
        shoppingCart.setCartItems(Set.of(cartItem));
        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto(getBookEntity().getId(), 1);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shoppingCartService.addItemToCart(USER_ID, cartItemRequestDto)
        );
        String expected = "You already have this book in your cart. Please, choose another book";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verifyNoMoreInteractions(shoppingCartRepository);
        verifyNoInteractions(cartItemMapper);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(cartItemRepository);
        verifyNoInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("""
            Update existing item quantity, returns shopping cart with changed cart item quantity
            """)
    void updateItemQuantity_ValidRequestDto_ReturnsChangedShoppingCart() {
        CartItemQuantityRequestDto requestDto = new CartItemQuantityRequestDto(CHANGED_QUANTITY);
        User user = new User(USER_ID);
        ShoppingCart shoppingCart = getShoppingCart(user);
        CartItem cartItem = getCartItem(shoppingCart);
        cartItem.setQuantity(requestDto.quantity());
        shoppingCart.setCartItems(Set.of(cartItem));
        ShoppingCartResponseDto shoppingCartResponseDto = getShoppingCartResponseDto();
        shoppingCartResponseDto.setCartItems(Set.of(new CartItemResponseDto(cartItem.getId(),
                getBookEntity().getId(), getBookEntity().getTitle(), requestDto.quantity())));
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(shoppingCartResponseDto);
        ShoppingCartResponseDto actual = shoppingCartService
                .updateItemQuantity(USER_ID, cartItem.getId(), requestDto);
        Assertions.assertEquals(shoppingCartResponseDto, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
    }

    @Test
    @DisplayName("Update item quantity, item is not found, returns exception")
    void updateItemQuantity_ItemNotFound_ReturnsException() {
        CartItemQuantityRequestDto requestDto = new CartItemQuantityRequestDto(CHANGED_QUANTITY);
        User user = new User(USER_ID);
        ShoppingCart shoppingCart = getShoppingCart(user);
        when(shoppingCartRepository.findByUserId(anyLong())).thenReturn(Optional.of(shoppingCart));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.updateItemQuantity(USER_ID, 1L, requestDto));
        String expected = "Can't find cart item with id: " + 1 + " in your shopping cart";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verifyNoMoreInteractions(shoppingCartRepository);
        verifyNoInteractions(cartItemRepository);
        verifyNoInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("Remove item from shopping cart, returns changed shopping cart")
    void removeBookFromShoppingCart_ValidItem_ReturnsChangedCart() {
        User user = new User(USER_ID);
        ShoppingCart shoppingCart = getShoppingCart(user);
        CartItem cartItem = getCartItem(shoppingCart);
        Set<CartItem> items = new HashSet<>();
        items.add(cartItem);
        shoppingCart.setCartItems(items);
        ShoppingCartResponseDto shoppingCartResponseDto = getShoppingCartResponseDto();
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(shoppingCartResponseDto);
        ShoppingCartResponseDto actual = shoppingCartService
                .removeBookFromShoppingCart(USER_ID, cartItem.getId());
        Assertions.assertEquals(shoppingCartResponseDto, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
    }

    @Test
    @DisplayName("Remove item from shopping cart, item is not found, returns exception")
    void removeBookFromShoppingCart_ItemNotFound_ReturnsException() {
        User user = new User(USER_ID);
        ShoppingCart shoppingCart = getShoppingCart(user);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.removeBookFromShoppingCart(USER_ID, 1L));
        String expected = "Can't find cart item with id: " + 1 + " in your shopping cart";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(USER_ID);
        verifyNoMoreInteractions(shoppingCartRepository);
        verifyNoInteractions(cartItemRepository);
        verifyNoInteractions(shoppingCartMapper);
    }

    private ShoppingCart getShoppingCart(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(USER_ID);
        shoppingCart.setUser(user);
        Set<CartItem> cartItems = new HashSet<>();
        shoppingCart.setCartItems(cartItems);
        shoppingCart.setDeleted(false);
        return shoppingCart;
    }

    private ShoppingCartResponseDto getShoppingCartResponseDto() {
        ShoppingCartResponseDto shoppingCartResponseDto = new ShoppingCartResponseDto();
        shoppingCartResponseDto.setId(USER_ID);
        shoppingCartResponseDto.setUserId(USER_ID);
        Set<CartItemResponseDto> itemResponseDtos = new HashSet<>();
        shoppingCartResponseDto.setCartItems(itemResponseDtos);
        return shoppingCartResponseDto;
    }

    private CartItem getCartItem(ShoppingCart shoppingCart) {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setBook(getBookEntity());
        cartItem.setQuantity(QUANTITY);
        cartItem.setDeleted(false);
        return cartItem;
    }

    private Book getBookEntity() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Witcher. The Last Wish");
        book.setAuthor("Andrzej Sapkowski");
        book.setIsbn("9780316333528");
        book.setPrice(BigDecimal.valueOf(19));
        book.setDescription("Magic adventures");
        book.setCoverImage("https://m.media-amazon.com/images/I/81MTXlALp+L._SL1500_.jpg");
        book.setCategories(Set.of(new Category(1L)));
        return book;
    }
}
