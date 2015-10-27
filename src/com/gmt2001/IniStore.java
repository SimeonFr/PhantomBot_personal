/* 
 * Copyright (C) 2015 www.phantombot.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmt2001;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Timer;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author gmt2001
 */
public class IniStore extends DataStore implements ActionListener
{

    private final HashMap<String, IniFile> files = new HashMap<>();
    private final HashMap<String, Date> changed = new HashMap<>();
    private final Date nextSave = new Date(0);
    private final Timer t;
    private final Timer t2;
    private static final long saveInterval = 5 * 60 * 1000;
    private static final IniStore instance = new IniStore();
    private String inifolder = "";

    public static IniStore instance()
    {
        return instance;
    }

    private IniStore()
    {
        inifolder = LoadConfigReal("");

        t = new Timer((int) saveInterval, this);
        t2 = new Timer(1, this);

        Thread.setDefaultUncaughtExceptionHandler(com.gmt2001.UncaughtExceptionHandler.instance());

        t.start();
    }
    
    private String validatefName(String fName)
    {
        fName = fName.replaceAll("([^a-zA-Z0-9_-])", "_");
        
        return fName;
    }
    
    private String validateSection(String section)
    {
        section = section.replaceAll("([^a-zA-Z0-9_-])", "_");
        
        return section;
    }
    
    private String validateKey(String key)
    {
        key = key.replaceAll("=", "_eq_");

        if (key.startsWith(";") || key.startsWith("["))
        {
            key = "_" + key;
        }
        
        return key;
    }

    private boolean LoadFile(String fName, boolean force)
    {
        fName = validatefName(fName);
        
        if (!files.containsKey(fName) || force)
        {
            try
            {
                String data = FileUtils.readFileToString(new File("./" + inifolder + "/" + fName + ".ini"));
                String[] lines = data.replaceAll("\\r", "").split("\\n");

                IniFile f = new IniFile();

                String section = "";

                f.data.put(section, new HashMap<String, String>());

                for (String line : lines)
                {
                    if (!line.trim().startsWith(";"))
                    {
                        if (line.trim().startsWith("[") && line.trim().endsWith("]"))
                        {
                            section = line.trim().substring(1, line.trim().length() - 1);
                            f.data.put(section, new HashMap<String, String>());
                        } else if (!line.trim().isEmpty())
                        {
                            String[] spl = line.split("=", 2);
                            f.data.get(section).put(spl[0], spl[1]);
                        }
                    }
                }

                files.put(fName, f);
            } catch (IOException ex)
            {
                IniFile f = new IniFile();
                f.data.put("", new HashMap<String, String>());

                files.put(fName, f);
                return false;
            }
        }

        return true;
    }

    private void SaveFile(String fName, IniFile data)
    {
        try
        {
            String wdata = "";
            Object[] adata = data.data.keySet().toArray();
            Object[] akdata;
            Object[] avdata;

            for (int i = 0; i < adata.length; i++)
            {
                if (i > 0)
                {
                    wdata += "\r\n";
                }

                if (!((String) adata[i]).equals(""))
                {
                    wdata += "[" + ((String) adata[i]) + "]\r\n";
                }

                akdata = data.data.get(((String) adata[i])).keySet().toArray();
                avdata = data.data.get(((String) adata[i])).values().toArray();

                for (int b = 0; b < akdata.length; b++)
                {
                    wdata += ((String) akdata[b]) + "=" + ((String) avdata[b]) + "\r\n";
                }
            }
            if (!Files.isDirectory(Paths.get("./" + inifolder + "/")))
            {
                Files.createDirectory(Paths.get("./" + inifolder + "/"));
            }

            Files.write(Paths.get("./" + inifolder + "/" + fName + ".ini"), wdata.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            changed.remove(fName);
        } catch (IOException ex)
        {
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        t2.stop();
        SaveAll(false);
    }

    private static class IniFile
    {

        protected HashMap<String, HashMap<String, String>> data = new HashMap<>();
    }

    @Override
    public void SaveChangedNow()
    {
        nextSave.setTime(new Date().getTime() - 1);

        SaveAll(false);
    }

    @Override
    public void SaveAll(boolean force)
    {
        if (!nextSave.after(new Date()) || force)
        {
            Object[] n = changed.keySet().toArray();
            if (n != null)
            {

                if (force)
                {
                    n = files.keySet().toArray();
                }

                com.gmt2001.Console.out.println(">>>Saving " + n.length + " files");

                for (Object n1 : n)
                {
                    try
                    {
                        if (force || changed.get((String) n1).after(nextSave) || changed.get((String) n1).equals(nextSave))
                        {
                            SaveFile((String) n1, files.get((String) n1));
                        }
                    } catch (java.lang.NullPointerException e)
                    {
                        try
                        {
                            SaveFile((String) n1, files.get((String) n1));
                        } catch (java.lang.NullPointerException e2)
                        {
                        }
                    }
                }

                nextSave.setTime(new Date().getTime() + saveInterval);

                com.gmt2001.Console.out.println(">>>Save complete");
            } else
            {
                com.gmt2001.Console.out.println(">>>Object null, nothing to save.");
            }
        }
    }

    @Override
    public void ReloadFile(String fName)
    {
        fName = validatefName(fName);
        
        LoadFile(fName, true);
    }

    @Override
    public void LoadConfig(String configStr)
    {
        inifolder = LoadConfigReal(configStr);
    }
    
    private static String LoadConfigReal(String configStr)
    {
        if (configStr.isEmpty())
        {
            return "inistore";
        } else
        {
            return configStr;
        }
    }

    @Override
    public String[] GetFileList()
    {
        Collection<File> f = FileUtils.listFiles(new File("./" + inifolder + "/"), null, false);

        String[] s = new String[f.size()];

        Iterator it = f.iterator();
        int i = 0;

        while (it.hasNext())
        {
            s[i++] = ((File) it.next()).getName();
        }

        return s;
    }

    @Override
    public String[] GetCategoryList(String fName)
    {
        fName = validatefName(fName);
        
        if (!LoadFile(fName, false))
        {
            return new String[]
            {
            };
        }

        Set<String> o = files.get(fName).data.keySet();

        String[] s = new String[o.size()];

        Iterator it = o.iterator();
        int i = 0;

        while (it.hasNext())
        {
            s[i++] = (String) it.next();
        }

        return s;
    }

    @Override
    public String[] GetKeyList(String fName, String section)
    {
        fName = validatefName(fName);
        
        if (!LoadFile(fName, false))
        {
            return new String[]
            {
            };
        }
        
        section = validateSection(section);

        Set<String> o = files.get(fName).data.get(section).keySet();

        String[] s = new String[o.size()];

        Iterator it = o.iterator();
        int i = 0;

        while (it.hasNext())
        {
            s[i++] = (String) it.next();
        }

        return s;
    }

    @Override
    public String GetString(String fName, String section, String key)
    {
        fName = validatefName(fName);
        
        if (!LoadFile(fName, false))
        {
            return null;
        }

        section = validateSection(section);
        key = validateKey(key);

        if (!files.containsKey(fName) || !files.get(fName).data.containsKey(section)
                || !files.get(fName).data.get(section).containsKey(key))
        {
            return null;
        }

        return (String) files.get(fName).data.get(section).get(key);
    }

    @Override
    public void SetString(String fName, String section, String key, String value)
    {
        fName = validatefName(fName);
        
        LoadFile(fName, false);

        section = validateSection(section);
        key = validateKey(key);

        if (!files.get(fName).data.containsKey(section))
        {
            files.get(fName).data.put(section, new HashMap<String, String>());
        }

        files.get(fName).data.get(section).put(key, value);

        changed.put(fName, new Date());

        t2.start();
    }

    @Override
    public int GetInteger(String fName, String section, String key)
    {
        String sval = GetString(fName, section, key);

        try
        {
            return Integer.parseInt(sval);
        } catch (Exception ex)
        {
            return 0;
        }
    }

    @Override
    public void SetInteger(String fName, String section, String key, int value)
    {
        String sval = Integer.toString(value);

        SetString(fName, section, key, sval);
    }

    @Override
    public float GetFloat(String fName, String section, String key)
    {
        String sval = GetString(fName, section, key);

        try
        {
            return Float.parseFloat(sval);
        } catch (Exception ex)
        {
            return 0.0f;
        }
    }

    @Override
    public void SetFloat(String fName, String section, String key, float value)
    {
        String sval = Float.toString(value);

        SetString(fName, section, key, sval);
    }

    @Override
    public double GetDouble(String fName, String section, String key)
    {
        String sval = GetString(fName, section, key);

        try
        {
            return Double.parseDouble(sval);
        } catch (Exception ex)
        {
            return 0.0;
        }
    }

    @Override
    public void SetDouble(String fName, String section, String key, double value)
    {
        String sval = Double.toString(value);

        SetString(fName, section, key, sval);
    }

    @Override
    public Boolean GetBoolean(String fName, String section, String key)
    {
        int ival = GetInteger(fName, section, key);

        return ival == 1;
    }

    @Override
    public void SetBoolean(String fName, String section, String key, Boolean value)
    {
        int ival = 0;

        if (value)
        {
            ival = 1;
        }

        SetInteger(fName, section, key, ival);
    }

    @Override
    public void RemoveKey(String fName, String section, String key)
    {
        fName = validatefName(fName);
        
        LoadFile(fName, false);

        section = validateSection(section);
        key = validateKey(key);

        files.get(fName).data.get(section).remove(key);

        SaveFile(fName, files.get(fName));
    }

    @Override
    public void RemoveSection(String fName, String section)
    {
        fName = validatefName(fName);
        
        LoadFile(fName, false);
        
        section = validateSection(section);

        files.get(fName).data.remove(section);

        SaveFile(fName, files.get(fName));
    }

    @Override
    public void RemoveFile(String fName)
    {
        fName = validatefName(fName);
        
        File f = new File("./" + inifolder + "/" + fName + ".ini");

        f.delete();
    }

    @Override
    public boolean FileExists(String fName)
    {
        fName = validatefName(fName);
     
        File f = new File("./" + inifolder + "/" + fName + ".ini");

        return f.exists();
    }

    @Override
    public boolean HasKey(String fName, String section, String key)
    {
        fName = validatefName(fName);
        section = validateSection(section);
        key = validateKey(key);
        
        return GetString(fName, section, key) != null;
    }

    @Override
    public boolean exists(String type, String key)
    {
        return HasKey(type, "", key);
    }

    @Override
    public String get(String type, String key)
    {
        return GetString(type, "", key);
    }

    @Override
    public void set(String type, String key, String value)
    {
        SetString(type, "", key, value);
        SaveFile(type, files.get(type));
    }

    @Override
    public void del(String type, String key)
    {
        RemoveKey(type, "", key);
    }

    @Override
    public void incr(String type, String key, int amount)
    {
        int ival = GetInteger(type, "", key);

        ival += amount;

        SetInteger(type, "", key, ival);
    }

    @Override
    public void decr(String type, String key, int amount)
    {
        int ival = GetInteger(type, "", key);

        ival -= amount;

        SetInteger(type, "", key, ival);
    }
}
