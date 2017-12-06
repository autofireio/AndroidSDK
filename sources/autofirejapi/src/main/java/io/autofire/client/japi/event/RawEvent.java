package io.autofire.client.japi.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RawEvent implements Serializable {
    public String name;
    public String timestamp;
    public HashMap<String, String> nominals;
    public HashMap<String, Integer> integrals;
    public HashMap<String, Double> fractionals;

    @Override
    public String toString() {
        int i;
        StringBuilder sb = new StringBuilder("(Event::" + name + " " + timestamp + " ");
        sb.append("(Nominals (");
        i = 0;
        for (Map.Entry<String, String> entry : nominals.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (i > 0)
                sb.append(" ");
            sb.append("(").append(k).append(" ").append(v).append(")");
            i++;
        }
        sb.append(")) ");
        sb.append("(Integrals (");
        i = 0;
        for (Map.Entry<String, Integer> entry : integrals.entrySet()) {
            String k = entry.getKey();
            int v = entry.getValue();
            if (i > 0)
                sb.append(" ");
            sb.append("(").append(k).append(" ").append(v).append(")");
            i++;
        }
        sb.append(")) ");
        sb.append("(Fractionals (");
        i = 0;
        for (Map.Entry<String, Double> entry : fractionals.entrySet()) {
            String k = entry.getKey();
            double v = entry.getValue();
            if (i > 0)
                sb.append(" ");
            sb.append("(").append(k).append(" ").append(v).append(")");
            i++;
        }
        sb.append(")))");

        return sb.toString();
    }
}
