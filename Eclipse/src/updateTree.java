import jxl.write.WriteException;

import java.awt.*;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.JPanel;

import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JProgressBar;


public class updateTree implements Runnable {
    private  Interface I;

    public updateTree(Interface I) {
        this.I = I;

    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(2*60*1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            I.updateTree();
        }
    }
}