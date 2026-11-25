import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.print.PrinterJob;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.List;

public class InvoicePage {

    public static void show(
            Stage stage, User user, int orderId) {

        if (!CustomerDashboard.isCustomer(stage, user)) {
            return;
        }

        try {

            // Verify customer ownership

            Order order = CustomerOrderService.getOrder(
                user.getUserId(), orderId);

            if (order == null) {

                UIComponents.error(
                    "Order not found or access denied.");

                MyOrders.show(stage, user);
                return;
            }

            List<OrderItem> items =
                CustomerOrderService.getOrderItems(
                    user.getUserId(), orderId);

            String invoiceText =
                InvoiceService.generateInvoice(
                    orderId, user.getUserId());

            String invoiceNumber =
                String.format("INV-%06d", orderId);

            // ================= NAVIGATION =================

            Button back = UIComponents.secondaryButton(
                "← Back to My Orders");

            Button save = UIComponents.secondaryButton(
                "Save Invoice");

            Button print = UIComponents.primaryButton(
                "Print Invoice");

            back.setOnAction(e ->
                MyOrders.show(stage, user));

            save.setOnAction(e ->
                saveInvoice(
                    stage, user, order,
                    items, invoiceText));

            print.setOnAction(e ->
                printInvoice(stage, invoiceText));

            Region space = new Region();

            HBox.setHgrow(space, Priority.ALWAYS);

            HBox navigation = new HBox(
                12, back, space, save, print);

            navigation.setAlignment(Pos.CENTER_LEFT);

            // ================= INVOICE HEADER =================

            VBox title = new VBox(

                6,

                styledLabel("INVOICE", "ip-title"),

                UIComponents.mutedText(invoiceNumber)
            );

            title.setAlignment(Pos.CENTER_RIGHT);

            Region headerSpace = new Region();

            HBox.setHgrow(
                headerSpace, Priority.ALWAYS);

            HBox header = new HBox(

                20,

                UIComponents.brand(false),

                headerSpace,

                title
            );

            header.setAlignment(Pos.CENTER_LEFT);

            Region greenLine = new Region();

            greenLine.getStyleClass().add("ip-green-line");

            // ================= ORDER INFORMATION =================

            GridPane orderInfo = twoColumnGrid();

            orderInfo.add(

                information(
                    "Invoice Number", invoiceNumber),

                0, 0
            );

            orderInfo.add(

                information(
                    "Order Date", MyOrders.formatDate(order)),

                1, 0
            );

            orderInfo.add(

                information(
                    "Order ID", "#" + orderId),

                0, 1
            );

            VBox status = new VBox(

                7,

                UIComponents.mutedText("Order Status"),

                UIComponents.orderBadge(order.getStatus())
            );

            orderInfo.add(status, 1, 1);

            // ================= CUSTOMER DETAILS =================

            VBox customer = new VBox(

                10,

                UIComponents.cardHeading(
                    "Customer Details"),

                information(
                    "Customer Name",
                    order.getCustomerName()),

                information(
                    "Email Address",
                    user.getEmail()),

                information(
                    "Customer ID",
                    "#" + user.getUserId())
            );

            VBox delivery = new VBox(

                10,

                UIComponents.cardHeading(
                    "Delivery Address"),

                information(
                    "Shipping Address",
                    order.getDeliveryAddress())
            );

            GridPane customerInfo = twoColumnGrid();

            customerInfo.add(customer, 0, 0);

            customerInfo.add(delivery, 1, 0);

            // ================= PAYMENT DETAILS =================

            VBox paymentMethod = information(

                "Payment Method",

                order.getPaymentMethod()
            );

            VBox paymentStatus = new VBox(

                7,

                UIComponents.mutedText("Payment Status"),

                OrderManagement.paymentBadge(
                    order.getPaymentStatus())
            );

            GridPane paymentInfo = twoColumnGrid();

            paymentInfo.add(paymentMethod, 0, 0);

            paymentInfo.add(paymentStatus, 1, 0);

            paymentInfo.getStyleClass().add(
                "ip-payment-panel");

            // ================= PURCHASED PRODUCTS =================

            GridPane productTable = createProductTable(items);

            BigDecimal subtotal = BigDecimal.ZERO;

            int totalQuantity = 0;

            for (OrderItem item : items) {

                subtotal = subtotal.add(
                    item.getSubtotal());

                totalQuantity += item.getQuantity();
            }

            // ================= ORDER SUMMARY =================

            VBox summary = new VBox(12);

            summary.setMaxWidth(390);

            summary.getChildren().addAll(

                CheckoutPage.summaryRow(
                    "Total Items",
                    String.valueOf(totalQuantity)),

                CheckoutPage.summaryRow(
                    "Subtotal",
                    UIComponents.currency(subtotal))
            );

            BigDecimal difference =
                order.getTotalAmount().subtract(subtotal);

            if (difference.signum() != 0) {

                summary.getChildren().add(

                    CheckoutPage.summaryRow(

                        "Order Amount Difference",

                        UIComponents.currency(difference))
                );

            } else {

                summary.getChildren().add(

                    CheckoutPage.summaryRow(
                        "Delivery Charges", "FREE")
                );
            }

            Label grandTotal = styledLabel(

                UIComponents.currency(
                    order.getTotalAmount()),

                "ip-grand-total"
            );

            Region totalSpace = new Region();

            HBox.setHgrow(
                totalSpace, Priority.ALWAYS);

            HBox totalRow = new HBox(

                15,

                styledLabel(
                    "Grand Total", "ip-total-label"),

                totalSpace,

                grandTotal
            );

            totalRow.setAlignment(Pos.CENTER_LEFT);

            totalRow.getStyleClass().add(
                "ip-total-panel");

            summary.getChildren().addAll(

                new Separator(),

                totalRow
            );

            HBox summaryContainer = new HBox(summary);

            summaryContainer.setAlignment(
                Pos.CENTER_RIGHT);

            // ================= INVOICE FOOTER =================

            VBox footer = new VBox(

                8,

                styledLabel(
                    "Thank you for shopping with A2Z!",
                    "ip-thank-you"),

                UIComponents.mutedText(
                    "Computer-generated invoice"),

                UIComponents.mutedText(
                    "Academic demonstration only. "
                    + "Not a statutory tax invoice.")
            );

            footer.setAlignment(Pos.CENTER);

            // ================= INVOICE DOCUMENT =================

            VBox invoice = new VBox(

                25,

                header,

                greenLine,

                orderInfo,

                new Separator(),

                customerInfo,

                paymentInfo,

                UIComponents.cardHeading(
                    "Purchased Products"),

                productTable,

                summaryContainer,

                new Separator(),

                footer
            );

            invoice.getStyleClass().add(
                "ip-document");

            invoice.setMaxWidth(950);

            HBox documentContainer = new HBox(invoice);

            documentContainer.setAlignment(Pos.TOP_CENTER);

            VBox content = new VBox(

                22,

                UIComponents.mutedText(
                    "Home / My Orders / Invoice"),

                UIComponents.heading("Order Invoice"),

                UIComponents.mutedText(
                    "View, save or print your purchase invoice."),

                navigation,

                documentContainer
            );

            CheckoutPage.showPage(

                stage,

                user,

                content,

                "A2Z - Order Invoice"
            );

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to generate invoice.");

            ex.printStackTrace();
        }
    }

    // =====================================================
    // PRODUCT TABLE
    // =====================================================

    private static GridPane createProductTable(
            List<OrderItem> items) {

        GridPane table = new GridPane();

        table.getStyleClass().add("ip-table");

        double[] widths = {8, 38, 12, 20, 22};

        for (double width : widths) {

            ColumnConstraints column =
                new ColumnConstraints();

            column.setPercentWidth(width);

            table.getColumnConstraints().add(column);
        }

        String[] headings = {

            "No.",
            "Product",
            "Qty",
            "Unit Price",
            "Subtotal"
        };

        for (int i = 0; i < headings.length; i++) {

            addCell(
                table, headings[i], i, 0, true);
        }

        int row = 1;

        for (OrderItem item : items) {

            addCell(
                table, String.valueOf(row),
                0, row, false);

            addCell(
                table, item.getProductName(),
                1, row, false);

            addCell(
                table, String.valueOf(item.getQuantity()),
                2, row, false);

            addCell(
                table,
                UIComponents.currency(item.getPrice()),
                3, row, false);

            addCell(
                table,
                UIComponents.currency(item.getSubtotal()),
                4, row, false);

            row++;
        }

        if (items.isEmpty()) {

            Label empty = UIComponents.mutedText(
                "No purchased products found.");

            empty.setPadding(new Insets(15));

            table.add(empty, 0, 1, 5, 1);
        }

        return table;
    }

    private static void addCell(

            GridPane table,
            String value,
            int column,
            int row,
            boolean heading) {

        Label label = new Label(value);

        label.setWrapText(true);

        label.setMaxWidth(Double.MAX_VALUE);

        label.setMaxHeight(Double.MAX_VALUE);

        label.setAlignment(

            column >= 2
                ? Pos.CENTER_RIGHT
                : Pos.CENTER_LEFT
        );

        label.getStyleClass().add(

            heading
                ? "ip-table-heading"
                : "ip-table-cell"
        );

        if (!heading && row % 2 == 0) {

            label.getStyleClass().add(
                "ip-alternate-row");
        }

        table.add(label, column, row);
    }

    // =====================================================
    // INFORMATION COMPONENTS
    // =====================================================

    private static VBox information(
            String title, String value) {

        Label heading =
            UIComponents.mutedText(title);

        Label text = styledLabel(

            value == null || value.isBlank()
                ? "Not available"
                : value,

            "ip-information-value"
        );

        text.setWrapText(true);

        return new VBox(7, heading, text);
    }

    private static Label styledLabel(
            String text, String styleClass) {

        Label label = new Label(text);

        label.getStyleClass().add(styleClass);

        return label;
    }

    private static GridPane twoColumnGrid() {

        GridPane grid = new GridPane();

        grid.setHgap(25);

        grid.setVgap(20);

        ColumnConstraints first =
            new ColumnConstraints();

        first.setPercentWidth(50);

        ColumnConstraints second =
            new ColumnConstraints();

        second.setPercentWidth(50);

        grid.getColumnConstraints().addAll(
            first, second);

        return grid;
    }

    // =====================================================
    // SAVE INVOICE
    // =====================================================

    private static void saveInvoice(

            Stage stage,
            User user,
            Order order,
            List<OrderItem> items,
            String invoiceText) {

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Save A2Z Invoice");

        chooser.setInitialFileName(

            "A2Z_Invoice_"
            + order.getOrderId()
            + ".html"
        );

        FileChooser.ExtensionFilter htmlFilter =
            new FileChooser.ExtensionFilter(
                "HTML Invoice", "*.html");

        FileChooser.ExtensionFilter textFilter =
            new FileChooser.ExtensionFilter(
                "Text Invoice", "*.txt");

        chooser.getExtensionFilters().addAll(
            htmlFilter, textFilter);

        chooser.setSelectedExtensionFilter(htmlFilter);

        File file = chooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        try {

            boolean html =
                chooser.getSelectedExtensionFilter()
                    == htmlFilter;

            String extension = html ? ".html" : ".txt";

            if (!file.getName().toLowerCase()
                    .endsWith(extension)) {

                file = new File(
                    file.getAbsolutePath() + extension);
            }

            String content = html

                ? createHtml(user, order, items)

                : invoiceText;

            Files.writeString(

                file.toPath(),

                content,

                StandardCharsets.UTF_8
            );

            UIComponents.success(
                "Invoice saved successfully!");

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to save invoice.");

            ex.printStackTrace();
        }
    }

    // =====================================================
    // PROFESSIONAL HTML INVOICE
    // =====================================================

    private static String createHtml(

            User user,
            Order order,
            List<OrderItem> items) {

        StringBuilder html = new StringBuilder();

        html.append(
            "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'>"
            + "<title>A2Z Invoice</title>"
        );

        html.append(
            "<style>"
            + "body{background:#F7F7F5;"
            + "font-family:Arial,sans-serif;"
            + "color:#191C1B;padding:30px;}"

            + ".invoice{max-width:850px;margin:auto;"
            + "background:white;padding:40px;"
            + "border:1px solid #E0E3E0;"
            + "border-radius:12px;}"

            + "h1,h2{color:#0D5C4B;}"

            + ".header{display:flex;"
            + "justify-content:space-between;"
            + "border-bottom:3px solid #0D5C4B;"
            + "padding-bottom:20px;}"

            + ".grid{display:grid;"
            + "grid-template-columns:1fr 1fr;"
            + "gap:25px;margin:25px 0;}"

            + ".payment{background:#F7F7F5;"
            + "padding:15px;border-radius:8px;}"

            + "table{width:100%;"
            + "border-collapse:collapse;"
            + "margin-top:20px;}"

            + "th{background:#0D5C4B;color:white;"
            + "text-align:left;}"

            + "td,th{padding:14px;"
            + "border-bottom:1px solid #E0E3E0;}"

            + "td{overflow-wrap:anywhere;}"

            + "tr:nth-child(even){background:#F7F7F5;}"

            + ".total{text-align:right;"
            + "background:#E9F3EE;"
            + "padding:20px;margin-top:25px;"
            + "color:#0D5C4B;font-size:24px;"
            + "font-weight:bold;}"

            + ".footer{text-align:center;"
            + "margin-top:30px;color:#5C5F5D;}"

            + "@media print{body{padding:0;"
            + "background:white;}"
            + ".invoice{border:none;padding:15px;}"
            + "thead{display:table-header-group;}"
            + "tr{break-inside:avoid;}}"

            + "</style></head><body>"
            + "<div class='invoice'>"
        );

        html.append(
            "<div class='header'>"
            + "<div><h1>A2Z Commerce</h1>"
            + "<p>Everything from A to Z</p></div>"
            + "<div><h1>INVOICE</h1><p>"
        );

        html.append(
            String.format(
                "INV-%06d", order.getOrderId()));

        html.append("</p></div></div>");

        html.append("<div class='grid'><div>");

        html.append("<p><b>Order ID:</b> #")
            .append(order.getOrderId())
            .append("</p>");

        html.append("<p><b>Order Date:</b> ")
            .append(escape(MyOrders.formatDate(order)))
            .append("</p>");

        html.append("</div><div>");

        html.append("<p><b>Order Status:</b> ")
            .append(escape(order.getStatus()))
            .append("</p>");

        html.append("</div></div>");

        // Customer and delivery information

        html.append(
            "<div class='grid'><div>"
            + "<h2>Customer Details</h2><p>"
        );

        html.append(escape(order.getCustomerName()))
            .append("</p><p>")
            .append(escape(user.getEmail()))
            .append("</p></div>");

        html.append(
            "<div><h2>Delivery Address</h2><p>"
        );

        html.append(
            escape(order.getDeliveryAddress())
                .replace("\n", "<br>")
        );

        html.append("</p></div></div>");

        // Payment details

        html.append(
            "<div class='payment'>"
            + "<b>Payment Method:</b> "
        );

        html.append(escape(order.getPaymentMethod()));

        html.append("<p><b>Payment Status:</b> ");

        html.append(escape(order.getPaymentStatus()));

        html.append("</p></div>");

        // Purchased products

        html.append(
            "<h2>Purchased Products</h2>"
            + "<table><thead><tr>"
            + "<th>No.</th>"
            + "<th>Product</th>"
            + "<th>Qty</th>"
            + "<th>Unit Price</th>"
            + "<th>Subtotal</th>"
            + "</tr></thead><tbody>"
        );

        int number = 1;

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItem item : items) {

            subtotal = subtotal.add(item.getSubtotal());

            html.append("<tr><td>")
                .append(number++)
                .append("</td><td>")
                .append(escape(item.getProductName()))
                .append("</td><td>")
                .append(item.getQuantity())
                .append("</td><td>")
                .append(escape(
                    UIComponents.currency(item.getPrice())))
                .append("</td><td>")
                .append(escape(
                    UIComponents.currency(item.getSubtotal())))
                .append("</td></tr>");
        }

        html.append("</tbody></table>");

        html.append("<p style='text-align:right'>Subtotal: ")
            .append(escape(UIComponents.currency(subtotal)))
            .append("</p>");

        BigDecimal difference =
            order.getTotalAmount().subtract(subtotal);

        if (difference.signum() != 0) {

            html.append(
                "<p style='text-align:right'>"
                + "Order Amount Difference: "
            );

            html.append(
                escape(UIComponents.currency(difference))
            );

            html.append("</p>");

        } else {

            html.append(
                "<p style='text-align:right'>"
                + "Delivery Charges: FREE</p>"
            );
        }

        html.append("<div class='total'>Grand Total: ");

        html.append(
            escape(UIComponents.currency(
                order.getTotalAmount()))
        );

        html.append("</div>");

        html.append(
            "<div class='footer'>"
            + "<h3>Thank you for shopping with A2Z!</h3>"
            + "<p>Computer-generated invoice</p>"
            + "<p>Academic demonstration only. "
            + "Not a statutory tax invoice.</p>"
            + "</div></div></body></html>"
        );

        return html.toString();
    }

    // Prevent HTML injection in exported invoices

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        return value

            .replace("&", "&amp;")

            .replace("<", "&lt;")

            .replace(">", "&gt;")

            .replace("\"", "&quot;")

            .replace("'", "&#39;");
    }

    // =====================================================
    // PRINT INVOICE
    // =====================================================

    private static void printInvoice(
            Stage stage, String invoice) {

        PrinterJob job = null;

        try {

            job = PrinterJob.createPrinterJob();

            if (job == null) {

                UIComponents.warning(
                    "No printer available.");

                return;
            }

            if (!job.showPrintDialog(stage)) {

                job.cancelJob();
                return;
            }

            double width = job.getJobSettings()
                .getPageLayout()
                .getPrintableWidth();

            double height = job.getJobSettings()
                .getPageLayout()
                .getPrintableHeight();

            String[] lines = invoice.split("\n");

            StringBuilder page = new StringBuilder();

            boolean success = true;

            for (String line : lines) {

                Text test = new Text(

                    page.toString() + line + "\n"
                );

                test.setFont(
                    Font.font("Monospaced", 10));

                test.setWrappingWidth(width);

                if (test.getLayoutBounds().getHeight()
                        > height
                        && page.length() > 0) {

                    success = printPage(
                        job, page.toString(), width);

                    if (!success) {
                        break;
                    }

                    page.setLength(0);
                }

                page.append(line).append("\n");
            }

            if (success && page.length() > 0) {

                success = printPage(
                    job, page.toString(), width);
            }

            if (success && job.endJob()) {

                UIComponents.success(
                    "Invoice sent to printer.");

            } else {

                job.cancelJob();

                UIComponents.error(
                    "Unable to print invoice.");
            }

        } catch (Exception ex) {

            if (job != null) {
                job.cancelJob();
            }

            UIComponents.error("Printing failed.");

            ex.printStackTrace();
        }
    }

    private static boolean printPage(

            PrinterJob job,
            String content,
            double width) {

        Text text = new Text(content);

        text.setFont(
            Font.font("Monospaced", 10));

        text.setWrappingWidth(width);

        return job.printPage(text);
    }
}