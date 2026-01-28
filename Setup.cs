using System;
using System.Diagnostics;
using System.IO;

class Launcher {
    static void Main() {
        string batFile = "ONE_CLICK_SETUP.bat";
        if (!File.Exists(batFile)) {
            Console.ForegroundColor = ConsoleColor.Red;
            Console.WriteLine("Error: " + batFile + " not found!");
            Console.WriteLine("Please ensure this Setup.exe is in the same folder as the script.");
            Console.ResetColor();
            Console.ReadKey();
            return;
        }

        try {
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = batFile;
            startInfo.UseShellExecute = true; 
            startInfo.WorkingDirectory = AppDomain.CurrentDomain.BaseDirectory;

            Process.Start(startInfo);
        } catch (Exception ex) {
            Console.WriteLine("Error launching installer: " + ex.Message);
            Console.ReadKey();
        }
    }
}
