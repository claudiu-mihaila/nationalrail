package eu.claudiumihaila.nationalrail;

import com.thalesgroup.rtti._2007_10_10.ldb.commontypes.FilterType;
import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import com.thalesgroup.rtti._2016_02_16.ldb.GetBoardRequestParams;
import com.thalesgroup.rtti._2016_02_16.ldb.LDBServiceSoap;
import com.thalesgroup.rtti._2016_02_16.ldb.Ldb;
import com.thalesgroup.rtti._2016_02_16.ldb.StationBoardWithDetailsResponseType;
import com.thalesgroup.rtti._2016_02_16.ldb.types.ServiceItemWithCallingPoints;
import com.thalesgroup.rtti._2016_02_16.ldb.types.StationBoardWithDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by claudiumihaila on 09/02/2017.
 */
public class Request {
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    private static final Ldb ldb = new Ldb();
    private static final LDBServiceSoap serviceSoap = ldb.getLDBServiceSoap();

    public static void main(String[] args) {
        final Properties properties = new Properties();
        try (final InputStream is = Request.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(is);
        } catch (IOException e) {
            LOGGER.error("could not read application properties", e);
        }

        final AccessToken accessToken = new AccessToken();
        accessToken.setTokenValue(properties.getProperty("accessToken"));

        final GetBoardRequestParams params = new GetBoardRequestParams();
        params.setCrs(properties.getProperty("destinationCRS"));
        params.setNumRows(19);
        params.setFilterCrs(properties.getProperty("originCRS", null));
        params.setFilterType(FilterType.FROM);
        params.setTimeOffset(null);
        params.setTimeWindow(null);

        final StationBoardWithDetailsResponseType resp = serviceSoap.getArrBoardWithDetails(params, accessToken);
        final StationBoardWithDetails result = resp.getGetStationBoardResult();

        for (ServiceItemWithCallingPoints service : result.getTrainServices().getService()) {
            if (!Globals.ON_TIME.equals(service.getEta())) {
                final StringBuilder stringBuilder = new StringBuilder("");
                stringBuilder.append(service.getServiceID());
                stringBuilder.append(" ");
                stringBuilder.append(service.getOrigin().getLocation().get(0).getCrs());
                stringBuilder.append("/");
                stringBuilder.append(service.getOrigin().getLocation().get(0).getLocationName());
                stringBuilder.append(" - ");
                stringBuilder.append(service.getDestination().getLocation().get(0).getCrs());
                stringBuilder.append("/");
                stringBuilder.append(service.getDestination().getLocation().get(0).getLocationName());
                stringBuilder.append(" by ");
                stringBuilder.append(service.getOperator());
                stringBuilder.append(" eta ");
                stringBuilder.append(service.getSta());
                stringBuilder.append(" ata ");
                stringBuilder.append(service.getEta());
                stringBuilder.append(" ");
                stringBuilder.append(service.getDelayReason());
                stringBuilder.append(" ");
                stringBuilder.append(service.getCancelReason());

                LOGGER.info(stringBuilder.toString());
            }
        }
    }
}
