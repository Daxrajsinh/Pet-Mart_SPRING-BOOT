package com.example.demo.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Product;
import com.example.demo.model.CartInfo;
import com.example.demo.model.CustomerInfo;
import com.example.demo.model.ProductInfo;
import com.example.demo.service.ListResultService;
import com.example.demo.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class MainController {

   @Autowired
   private ProductDAO productDAO;

   @RequestMapping("/")
   public String home() {
      return "index";
   }

   // Product List
   @RequestMapping({ "/productList" })
   public String listProductHandler(Model model,
         @RequestParam(value = "name", defaultValue = "") String likeName,
         @RequestParam(value = "page", defaultValue = "1") int page) {
      final int maxResult = 100;

      ListResultService<ProductInfo> result = productDAO.queryProducts(page,maxResult, likeName);

      model.addAttribute("listProducts", result);
      return "productList";
   }


   @RequestMapping({ "/buyProduct" })
   public String listProductHandler(HttpServletRequest request, Model model,
         @RequestParam(value = "code", defaultValue = "") String code) {

      Product product = null;
      if (code != null && code.length() > 0) {
         product = productDAO.findProduct(code);
      }
      if (product != null) {

         //
         CartInfo cartInfo = CartService.getCartInSession(request);

         ProductInfo productInfo = new ProductInfo(product);

         cartInfo.addProduct(productInfo);
      }

      return "redirect:/shoppingCart";
   }


   @RequestMapping({ "/shoppingCartRemoveProduct" })
   public String removeProductHandler(HttpServletRequest request, Model model,
         @RequestParam(value = "code", defaultValue = "") String code) {
      Product product = null;
      if (code != null && code.length() > 0) {
         product = productDAO.findProduct(code);
      }
      if (product != null) {

         CartInfo cartInfo = CartService.getCartInSession(request);

         ProductInfo productInfo = new ProductInfo(product);

         cartInfo.removeProduct(productInfo);

      }

      return "redirect:/shoppingCart";
   }

   // GET: Show cart.
   @GetMapping({ "/shoppingCart" })
   public String shoppingCartHandler(HttpServletRequest request, Model model) {
      CartInfo myCart = CartService.getCartInSession(request);

      model.addAttribute("cartForm", myCart);
      return "shoppingCart";
   }

   // GET: Enter customer information.
   @GetMapping({ "/shoppingCartCustomer" })
   public String shoppingCartCustomerForm(HttpServletRequest request, Model model) {

      CartInfo cartInfo = CartService.getCartInSession(request);

      if (cartInfo.isEmpty()) {

         return "redirect:/shoppingCart";
      }
      CustomerInfo customerInfo = cartInfo.getCustomerInfo();

      Customer customer = new Customer(customerInfo);

      model.addAttribute("customerForm", customer);

      return "shoppingCartCustomer";
   }

   // POST: Save customer information.
   @PostMapping({ "/shoppingCartCustomer" })
   public String shoppingCartCustomerSave(HttpServletRequest request,
         Model model,
         @ModelAttribute("customerForm") @Validated Customer customer,
         BindingResult result,
         final RedirectAttributes redirectAttributes) {

      if (result.hasErrors()) {
         customer.setValid(false);
         // Forward to reenter customer info.
         return "shoppingCartCustomer";
      }

      customer.setValid(true);
      CartInfo cartInfo = CartService.getCartInSession(request);
      CustomerInfo customerInfo = new CustomerInfo(customer);
      cartInfo.setCustomerInfo(customerInfo);

      return "redirect:/shoppingCartConfirmation";
   }

   // GET: Show information to confirm.
   @GetMapping({ "/shoppingCartConfirmation" })
   public String shoppingCartConfirmationReview(HttpServletRequest request, Model model) {
      CartInfo cartInfo = CartService.getCartInSession(request);

      if (cartInfo == null || cartInfo.isEmpty()) {

         return "redirect:/shoppingCart";
      } else if (!cartInfo.isValidCustomer()) {

         return "redirect:/shoppingCartCustomer";
      }
      model.addAttribute("myCart", cartInfo);

      return "shoppingCartConfirmation";
   }

   // POST: Submit Cart (Save)
   @PostMapping({ "/shoppingCartConfirmation" })

   public String shoppingCartConfirmationSave(HttpServletRequest request, Model model) {
      CartInfo cartInfo = CartService.getCartInSession(request);

      if (cartInfo.isEmpty()) {

         return "redirect:/shoppingCart";
      } else if (!cartInfo.isValidCustomer()) {

         return "redirect:/shoppingCartCustomer";
      }
      try {
//         orderDAO.saveOrder(cartInfo);
      } catch (Exception e) {

         return "shoppingCartConfirmation";
      }

      // Remove Cart from Session.
      CartService.removeCartInSession(request);

      // Store last cart.
      CartService.storeLastOrderedCartInSession(request, cartInfo);

      return "redirect:/shoppingCartFinalize";
   }

   @GetMapping({ "/shoppingCartFinalize" })
   public String shoppingCartFinalize(HttpServletRequest request, Model model) {

      CartInfo lastOrderedCart = CartService.getLastOrderedCartInSession(request);

      if (lastOrderedCart == null) {
         return "redirect:/shoppingCart";
      }
      model.addAttribute("lastOrderedCart", lastOrderedCart);
      return "shoppingCartFinalize";
   }

   @GetMapping({ "/productImage" })
   public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
         @RequestParam("code") String code) throws IOException {
      Product product = null;
      if (code != null) {
         product = this.productDAO.findProduct(code);
      }
      if (product != null && product.getImage() != null) {
         response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
         response.getOutputStream().write(product.getImage());
      }
      response.getOutputStream().close();
   }

}
