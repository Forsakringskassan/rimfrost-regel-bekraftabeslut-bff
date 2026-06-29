package se.fk.github.bekraftabeslutbff;

import org.junit.jupiter.api.Test;
import se.fk.github.bekraftabeslutbff.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BekraftabeslutMapperTest
{

   @Test
   void transform_mapsTopLevelFields()
   {
      RawBekraftabeslutResponse raw = buildRaw("handlaggning-1");

      BekraftabeslutResponse result = BekraftabeslutMapper.transform(raw);

      assertEquals("handlaggning-1", result.handlaggningId);
   }

   @Test
   void transform_mapsKundFields()
   {
      RawBekraftabeslutResponse raw = buildRaw("h-1");

      BekraftabeslutResponse result = BekraftabeslutMapper.transform(raw);

      assertEquals("Anna", result.kund.fornamn);
      assertEquals("Svensson", result.kund.efternamn);
      assertEquals("K", result.kund.kon);
   }

   @Test
   void transform_mapsAnstallningFields()
   {
      RawBekraftabeslutResponse raw = buildRaw("h-1");

      BekraftabeslutResponse result = BekraftabeslutMapper.transform(raw);

      assertNotNull(result.kund.anstallning);
      assertEquals("2020-01-01", result.kund.anstallning.anstallningsdag);
      assertEquals(100, result.kund.anstallning.arbetstidProcent);
      assertEquals("2025-12-31", result.kund.anstallning.sistaAnstallningsdag);
      assertEquals("Bolaget AB", result.kund.anstallning.organisationsnamn);
      assertEquals("556000-0000", result.kund.anstallning.organisationsnummer);
   }

   @Test
   void transform_nullAnstallning_leavesAnstallningNull()
   {
      RawBekraftabeslutResponse raw = buildRaw("h-1");
      raw.kund.anstallning = null;

      BekraftabeslutResponse result = BekraftabeslutMapper.transform(raw);

      assertNull(result.kund.anstallning);
   }

   @Test
   void transform_mapsErsattningFields()
   {
      RawBekraftabeslutResponse raw = buildRaw("h-1");

      BekraftabeslutResponse result = BekraftabeslutMapper.transform(raw);

      assertEquals(1, result.ersattning.size());
      Ersattning e = result.ersattning.get(0);
      assertEquals("ers-uuid-1", e.ersattningId);
      assertEquals("SGI", e.ersattningstyp);
      assertEquals(75, e.omfattningProcent);
      assertEquals(500, e.belopp);
      assertEquals("grundbelopp", e.berakningsgrund);
      assertEquals("BEVILJAD", e.beslutsutfall);
      assertNull(e.avslagsanledning);
      assertEquals("2024-01-01", e.from);
      assertEquals("2024-03-31", e.tom);
   }

   private RawBekraftabeslutResponse buildRaw(String handlaggningId)
   {
      RawAnstallning rawAnstallning = new RawAnstallning();
      rawAnstallning.anstallningsdag = "2020-01-01";
      rawAnstallning.arbetstidProcent = 100;
      rawAnstallning.sistaAnstallningsdag = "2025-12-31";
      rawAnstallning.organisationsnamn = "Bolaget AB";
      rawAnstallning.organisationsnummer = "556000-0000";

      RawKund rawKund = new RawKund();
      rawKund.fornamn = "Anna";
      rawKund.efternamn = "Svensson";
      rawKund.kon = "K";
      rawKund.anstallning = rawAnstallning;

      RawErsattning rawErsattning = new RawErsattning();
      rawErsattning.ersattningId = "ers-uuid-1";
      rawErsattning.ersattningstyp = "SGI";
      rawErsattning.omfattningProcent = 75;
      rawErsattning.belopp = 500;
      rawErsattning.berakningsgrund = "grundbelopp";
      rawErsattning.beslutsutfall = "BEVILJAD";
      rawErsattning.avslagsanledning = null;
      rawErsattning.from = "2024-01-01";
      rawErsattning.tom = "2024-03-31";

      RawBekraftabeslutResponse raw = new RawBekraftabeslutResponse();
      raw.handlaggningId = handlaggningId;
      raw.kund = rawKund;
      raw.ersattning = List.of(rawErsattning);
      return raw;
   }
}
