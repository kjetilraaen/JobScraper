import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

public class Main
{
    public static void main(String[] args) throws IOException, ParseException
    {
        JobAddParser parser=new JobAddParser();
        Map<Integer, JobAdd> adds = parser.readAdds("job-ads-utf8.txt");
        parser.parseAttr(adds, "job-ads-attr-utf8.txt");
        System.out.println("Parsing done.!");
        parser.writeITJobs(adds, "it_jobs.txt");
        //writeAllJobTitles(adds);
        return;
    }

}
