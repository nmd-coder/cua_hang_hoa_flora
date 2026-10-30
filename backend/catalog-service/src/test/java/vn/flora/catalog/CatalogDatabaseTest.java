package vn.flora.catalog;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
@SpringBootTest
@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS",matches="true")
class CatalogDatabaseTest {
    @Autowired ProductController catalog;
    @Autowired JdbcTemplate db;
    @Test void searchAndPaginationUseRealPostgres() {
        String key=UUID.randomUUID().toString();
        UUID first=UUID.randomUUID(), second=UUID.randomUUID();
        try {
            db.update("INSERT INTO catalog.products(id,sku,name,slug,price,status) VALUES (?,?,?,?,?,'ACTIVE')",first,key+"-1",key+" first",key+"-1",100);
            db.update("INSERT INTO catalog.products(id,sku,name,slug,price,status) VALUES (?,?,?,?,?,'ACTIVE')",second,key+"-2",key+" second",key+"-2",100);
            assertEquals(2,catalog.list(key,0,12).size());
            assertEquals(1,catalog.list(key,0,1).size());
            assertEquals(1,catalog.list(key,1,1).size());
            assertTrue(catalog.list(key+"%",0,12).isEmpty());
            assertThrows(IllegalArgumentException.class,()->catalog.list("",-1,12));
        } finally { db.update("DELETE FROM catalog.products WHERE id IN (?,?)",first,second); }
    }
}