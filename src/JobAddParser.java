import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * Created by kjetilr on 21/02/2018.
 */
public class JobAddParser
{
    private int hoplessCases = 0;

    public void writeITJobs(Map<Integer, JobAdd> adds, String fileName) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        int noProgrammingJobs=0;
        for (JobAdd jobAdd : adds.values())
        {
            if(jobAdd.isProgrammingJob())
            {
                noProgrammingJobs++;
                writer.append(jobAdd.makeStringForR());
            }
        }
        writer.close();
        System.out.println("Number of programming jobs = " + noProgrammingJobs);
    }

    private void writeAllJobTitles(Map<Integer, JobAdd> adds) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter("job_descriptions.txt"));
        int hasJobTypeAttr = 0;
        for (JobAdd jobAdd : adds.values())
        {
            String occupation = jobAdd.getAttr(2012);
            String title = jobAdd.getAttr(180);
            if (!(occupation.isEmpty() && title.isEmpty())) hasJobTypeAttr++;
            writer.append(jobAdd.getFinnkode() + ": " + occupation + " %% " + title + "\n");
        }
        System.out.println("Missing jobtype: " + (adds.size()-hasJobTypeAttr) + "/" + adds.size());
        writer.close();
    }

    public void parseAttr(Map<Integer, JobAdd> adds, String fileName) throws IOException
    {
        int failures = 0;
        int attrForMissingAdd = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while (reader.ready())
        {
            String[] tokens = reader.readLine().split("\t");
            int finnkode = Integer.parseInt(tokens[0]);
            JobAdd jobAdd = adds.get(finnkode);
            if (jobAdd == null)
            {
                attrForMissingAdd++;
                continue;
            }
            boolean ok = jobAdd.addAttr(tokens);
            if (!ok) failures++;

        }
        System.out.println("failures in attributes = " + failures);
        System.out.println("Attributes for missing adds = " + attrForMissingAdd);
    }


    public Map<Integer, JobAdd> readAdds(String fileName) throws IOException, ParseException
    {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        Map<Integer, JobAdd> adds = new LinkedHashMap<>();

        int currentLine = 0;
        while (reader.ready())
        {
            JobAdd jobAdd = parseAdd(currentLine, reader);
            adds.put(jobAdd.getFinnkode(), jobAdd);
            currentLine++;
        }
        System.out.println("Total jobs: " + (currentLine - 1) + " hopless cases: " + hoplessCases);
        reader.close();
        return adds;
    }

    private JobAdd parseAdd(int lineNo, BufferedReader reader) throws IOException, ParseException
    {
        List<String> tokens = new ArrayList<>();
        JobAdd currentAdd = new JobAdd();
        do //Some adds are written across multiple lines.
        {
            tokens.addAll(Arrays.asList(reader.readLine().trim().split("\t")));
        } while (tokens.size() < 10);

        while (!tokens.get(9).startsWith("Jobb") && !isNumeric(tokens.get(4))) //sometimes field 3 crosses a line, merge with next
        {
//            System.out.println("token merged = \"" + tokens.get(3) + "\"+\"" + tokens.get(4));
            tokens.set(3, tokens.get(3) + " " + tokens.get(4));
            tokens.remove(4);
        }

        if (tokens.size() != 10)
        {
//            System.out.println("Spurious lines: " + lineNo + " length: " + tokens.size());
            hoplessCases++;
            return currentAdd;

        }
        currentAdd.setValues(tokens);
        return currentAdd;
    }

    public boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\d+)?");  //match a number with optional '-'.
    }

}
