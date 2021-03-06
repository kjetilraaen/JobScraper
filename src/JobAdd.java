import org.jsoup.Jsoup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class JobAdd
{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.mm.dd");
    private static int[] relevantAttributes = {184, 189, 201, 204, 207, 210, 242,};
    private Map<Integer, StringBuilder> attributes;
    private int finnkode;
    private Date fromDate;
    private Date toDate;
    private String headline;
    private String postCode;
    private String orgName;

    public JobAdd()
    {
        attributes = new LinkedHashMap<>();
    }

    private static String cleanText(String str)
    {
        String text = Jsoup.parse(str).text();
        text = text.replace('\"', ' ');
        text = text.replace('\'', ' ');
        text = text.replace('\\', '/');
        text = text.replace(',', ' ');
        text = text.replace('+', 'p'); //Elastic search cannot handle escaping of plus characters
        return text;
    }

    void setValues(List<String> tokens) throws ParseException
    {
        finnkode = parseInt(tokens.get(0)); //Cannot be empty
        fromDate = dateFormat.parse(tokens.get(1)); //Cannot be empty
        if (!tokens.get(2).isEmpty()) toDate = dateFormat.parse(tokens.get(2));
        headline = tokens.get(3);
        postCode = tokens.get(4);
        orgName = tokens.get(6);
    }

    public int getFinnkode()
    {
        return finnkode;
    }

    public boolean addAttr(String[] tokens)
    {
        int attrKey = Integer.parseInt(tokens[1]);
        if (tokens.length != 6)
        {
            return false;
        }
        if (attributes.containsKey(attrKey))
        {
            attributes.get(attrKey).append(' ').append(tokens[5]);
        } else
        {
            attributes.put(attrKey, new StringBuilder(tokens[5]));
        }
        return true;
    }

    public String getAttr(int key)
    {
        StringBuilder stringBuilder = attributes.get(key);
        if (stringBuilder == null)
            return "";
        return stringBuilder.toString();
    }

    public boolean isProgrammingJob()
    {
        String occupation = (getAttr(2012) + " " + getAttr(180)).toLowerCase();
        return (occupation.contains("utvikl") &&
                !(occupation.contains("forretning") ||
                        occupation.contains("eiendom") ||
                        occupation.contains("kompetanse") ||
                        occupation.contains("organisasjon") ||
                        occupation.contains("konsept") ||
                        occupation.contains("hemmede") ||
                        occupation.contains("infratruktur") ||
                        occupation.contains("portefølje") ||
                        occupation.contains("prosjekt") ||
                        occupation.contains("fag") ||
                        occupation.contains("produkt")));
    }

    public String toJson()
    {
        StringBuilder builder = new StringBuilder("{\"index\":{\"_id\":\"" + finnkode + "\"}}\n");
        builder.append("{ \"date\": \"");
        builder.append(dateFormat.format(fromDate));
        builder.append("\", \"text\": \"");
        builder.append(cleanText(headline));
        writeMainDescription(builder);
        builder.append("\" }\n");
        return builder.toString();
    }

    public String toCSV()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(finnkode);
        builder.append(',');
        builder.append(dateFormat.format(fromDate));
        builder.append(",");
        builder.append(cleanText(headline));
        writeMainDescription(builder);
        builder.append('\n');
        return builder.toString();
    }

    private void writeMainDescription(StringBuilder builder)
    {
        for (int relevantAttribute : relevantAttributes)
        {
            if (attributes.containsKey(relevantAttribute))
            {
                builder.append(' ');
                String str = attributes.get(relevantAttribute).toString();
                builder.append(cleanText(str));

            }
        }
    }

    public boolean hasFromDate()
    {
        return fromDate != null;
    }
}
