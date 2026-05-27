package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Sale;
import com.bruno.barbershopapi.app.web.model.report.sales.SalesSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Sale, UUID> {

    // ══════════════════════════════════════════════════════════
    //  VENTAS — totales y métricas
    // ══════════════════════════════════════════════════════════

    @Query("""
    SELECT COALESCE(SUM(s.total), 0),
           COUNT(s),
           COALESCE(AVG(s.total), 0)
    FROM Sale s
    WHERE s.barbershop.id = :shopId
    AND s.status = 'completed'
    AND s.createdAt BETWEEN :from AND :to
""")
    List<Object[]> getSalesSummary(
            @Param("shopId") UUID shopId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    // Ventas agrupadas por día
    @Query(value = """
        SELECT DATE(s.created_at AT TIME ZONE 'America/Merida') AS day,
               COUNT(*)                                              AS total_sales,
               COALESCE(SUM(s.total), 0)                            AS revenue
        FROM sales s
        WHERE s.barbershop_id = :shopId
        AND s.status = 'completed'
        AND s.created_at BETWEEN :from AND :to
        GROUP BY day
        ORDER BY day ASC
    """, nativeQuery = true)
    List<Object[]> getSalesByDay(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to);

    // Ventas agrupadas por semana
    @Query(value = """
        SELECT DATE_TRUNC('week', s.created_at AT TIME ZONE 'America/Hermosillo') AS week_start,
               COUNT(*)                                                             AS total_sales,
               COALESCE(SUM(s.total), 0)                                           AS revenue
        FROM sales s
        WHERE s.barbershop_id = :shopId
        AND s.status = 'completed'
        AND s.created_at BETWEEN :from AND :to
        GROUP BY week_start
        ORDER BY week_start ASC
    """, nativeQuery = true)
    List<Object[]> getSalesByWeek(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to);

    // Ventas agrupadas por mes
    @Query(value = """
        SELECT TO_CHAR(s.created_at AT TIME ZONE 'America/Hermosillo', 'YYYY-MM') AS month,
               COUNT(*)                                                             AS total_sales,
               COALESCE(SUM(s.total), 0)                                           AS revenue
        FROM sales s
        WHERE s.barbershop_id = :shopId
        AND s.status = 'completed'
        AND s.created_at BETWEEN :from AND :to
        GROUP BY month
        ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> getSalesByMonth(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to);

    // ══════════════════════════════════════════════════════════
    //  SERVICIOS — los más vendidos
    // ══════════════════════════════════════════════════════════

    @Query(value = """
        SELECT si.item_name,
               SUM(si.quantity)   AS total_qty,
               SUM(si.total)      AS total_revenue
        FROM sale_items si
        JOIN sales s ON s.id = si.sale_id
        WHERE s.barbershop_id = :shopId
        AND s.status = 'completed'
        AND si.item_type = 'service'
        AND s.created_at BETWEEN :from AND :to
        GROUP BY si.item_name
        ORDER BY total_qty DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> getTopServices(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to,
            @Param("limit")  int limit);

    // ══════════════════════════════════════════════════════════
    //  BARBEROS — ventas y servicios por barbero
    // ══════════════════════════════════════════════════════════

    @Query(value = """
        SELECT u.full_name                AS barber_name,
               COUNT(DISTINCT s.id)       AS total_sales,
               COALESCE(SUM(s.total), 0)  AS revenue,
               COALESCE(SUM(si.quantity), 0) AS total_services
        FROM sales s
        JOIN users u ON u.id = s.attended_by_user_id
        LEFT JOIN sale_items si ON si.sale_id = s.id AND si.item_type = 'service'
        WHERE s.barbershop_id = :shopId
        AND s.status = 'completed'
        AND s.created_at BETWEEN :from AND :to
        GROUP BY u.id, u.full_name
        ORDER BY revenue DESC
    """, nativeQuery = true)
    List<Object[]> getSalesByBarber(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to);

    // ══════════════════════════════════════════════════════════
    //  CLIENTES — nuevos vs recurrentes
    // ══════════════════════════════════════════════════════════

    // Clientes nuevos en el período (primera venta dentro del rango)
    @Query(value = """
        SELECT COUNT(DISTINCT s.client_id)
        FROM sales s
        WHERE s.barbershop_id = :shopId
        AND s.status = 'completed'
        AND s.client_id IS NOT NULL
        AND s.created_at BETWEEN :from AND :to
        AND s.client_id NOT IN (
            SELECT DISTINCT s2.client_id
            FROM sales s2
            WHERE s2.barbershop_id = :shopId
            AND s2.status = 'completed'
            AND s2.client_id IS NOT NULL
            AND s2.created_at < :from
        )
    """, nativeQuery = true)
    Long getNewClientsCount(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to);

    // Clientes recurrentes (más de 1 visita en el período)
    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT s.client_id
            FROM sales s
            WHERE s.barbershop_id = :shopId
            AND s.status = 'completed'
            AND s.client_id IS NOT NULL
            AND s.created_at BETWEEN :from AND :to
            GROUP BY s.client_id
            HAVING COUNT(*) > 1
        ) recurring
    """, nativeQuery = true)
    Long getRecurringClientsCount(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to);

    // ══════════════════════════════════════════════════════════
    //  MÉTODOS DE PAGO
    // ══════════════════════════════════════════════════════════

    @Query(value = """
        SELECT s.payment_method,
               COUNT(*)                  AS total_sales,
               COALESCE(SUM(s.total), 0) AS revenue
        FROM sales s
        WHERE s.barbershop_id = :shopId
        AND s.status = 'completed'
        AND s.created_at BETWEEN :from AND :to
        GROUP BY s.payment_method
        ORDER BY revenue DESC
    """, nativeQuery = true)
    List<Object[]> getSalesByPaymentMethod(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to);

    // ══════════════════════════════════════════════════════════
    //  DÍAS CON MÁS VENTAS
    // ══════════════════════════════════════════════════════════

    @Query(value = """
        SELECT TO_CHAR(s.created_at AT TIME ZONE 'America/Hermosillo', 'Day') AS day_name,
               EXTRACT(DOW FROM s.created_at AT TIME ZONE 'America/Hermosillo') AS day_number,
               COUNT(*)                  AS total_sales,
               COALESCE(SUM(s.total), 0) AS revenue
        FROM sales s
        WHERE s.barbershop_id = :shopId
        AND s.status = 'completed'
        AND s.created_at BETWEEN :from AND :to
        GROUP BY day_name, day_number
        ORDER BY revenue DESC
    """, nativeQuery = true)
    List<Object[]> getSalesByDayOfWeek(
            @Param("shopId") UUID shopId,
            @Param("from")   OffsetDateTime from,
            @Param("to")     OffsetDateTime to);
}