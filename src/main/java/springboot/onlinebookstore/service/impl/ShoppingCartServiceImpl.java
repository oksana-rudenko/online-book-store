package springboot.onlinebookstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springboot.onlinebookstore.dto.cartitem.request.CartItemQuantityRequestDto;
import springboot.onlinebookstore.dto.cartitem.request.CartItemRequestDto;
import springboot.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import springboot.onlinebookstore.exception.EntityNotFoundException;
import springboot.onlinebookstore.mapper.CartItemMapper;
import springboot.onlinebookstore.mapper.ShoppingCartMapper;
import springboot.onlinebookstore.model.Book;
import springboot.onlinebookstore.model.CartItem;
import springboot.onlinebookstore.model.ShoppingCart;
import springboot.onlinebookstore.model.User;
import springboot.onlinebookstore.repository.book.BookRepository;
import springboot.onlinebookstore.repository.cartitem.CartItemRepository;
import springboot.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import springboot.onlinebookstore.repository.user.UserRepository;
import springboot.onlinebookstore.service.ShoppingCartService;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    public ShoppingCartResponseDto getCartByUser(Long userId) {
        if (shoppingCartRepository.findByUserId(userId).isPresent()) {
            return shoppingCartMapper.toDto(shoppingCartRepository.findByUserId(userId).get());
        }
        User user = userRepository.getReferenceById(userId);
        ShoppingCart shoppingCart = new ShoppingCart(user);
        shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartResponseDto addItemToCart(Long userId, CartItemRequestDto requestDto) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find shopping cart for user with id: " + userId
                ));
        if (shoppingCart.getCartItems().stream()
                .anyMatch(c -> c.getBook().getId().equals(requestDto.bookId()))) {
            throw new RuntimeException("You already have this book in your cart. "
                    + "Please, choose another book");
        }
        CartItem cartItem = cartItemMapper.toEntity(requestDto);
        cartItem.setShoppingCart(shoppingCart);
        Book book = bookRepository.findById(requestDto.bookId()).orElseThrow(() ->
                new EntityNotFoundException("Can't find book with id: "
                        + requestDto.bookId()));
        cartItem.setBook(book);
        cartItemRepository.save(cartItem);
        shoppingCart.getCartItems().add(cartItem);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartResponseDto updateItemQuantity(
            Long userId, Long cartItemId, CartItemQuantityRequestDto requestDto
    ) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find and update "
                        + "shopping cart for user with id: " + userId)
        );
        CartItem cartItem = shoppingCart.getCartItems().stream()
                .filter(c -> c.getId().equals(cartItemId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException(
                        "Can't find cart item with id: " + cartItemId + " in your shopping cart"
                ));
        cartItem.setQuantity(requestDto.quantity());
        cartItemRepository.save(cartItem);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartResponseDto removeBookFromShoppingCart(Long userId, Long cartItemId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find and update "
                        + "shopping cart for user with id: " + userId)
        );
        CartItem cartItem = shoppingCart.getCartItems().stream()
                .filter(c -> c.getId().equals(cartItemId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException(
                        "Can't find cart item with id: " + cartItemId + " in your shopping cart"
                ));
        cartItemRepository.delete(cartItem);
        shoppingCart.getCartItems().remove(cartItem);
        return shoppingCartMapper.toDto(shoppingCart);
    }
}
