"ORD-1707123456789"
```

### **2. Stock Management**
- Reduces stock when order created
- Restores stock when order cancelled
- Validates availability before purchase

### **3. Cart Auto-Clear**
- Clears cart after successful order
- No manual intervention needed

### **4. Order Lifecycle**
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
```

### **5. Payment Status**
- COD automatically marked PAID on delivery
- Other methods tracked separately

### **6. Security**
- Users can only view their own orders
- Admin endpoints protected with `@PreAuthorize`
- Stock validation prevents overselling

---

## 📡 **API Endpoints:**
```code
POST   /api/v1/orders                    - Create order
GET    /api/v1/orders/my-orders          - Get user's orders
GET    /api/v1/orders/{id}               - Get order by ID
GET    /api/v1/orders/number/{number}    - Get by order number
POST   /api/v1/orders/{id}/cancel        - Cancel order
PATCH  /api/v1/orders/{id}/status        - Update status (Admin)
GET    /api/v1/orders                    - Get all orders (Admin)
DELETE /api/v1/orders/{id}               - Delete order (Admin)
```