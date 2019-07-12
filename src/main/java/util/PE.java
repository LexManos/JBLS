package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

public class PE
{
	/** The offset of the PE section in the file */
	final static private int PE_START = 0x3c;
	/** The code that identifies the version */
	final static private int RT_VERSION = 16;
	/** The string that identifies rsrc (".rsrc\0\0\0") */
	final static private long rsrc = 0x000000637273722EL;

	/** This is the entry point to the whole file version function set.  It takes the filename as a parameter
	 * and returns the version.  If the version can't be returned for whatever reason, an IOException is
	 * thrown.
	 *
	 * @param filename The filename to get the version of.
	 * @return The version of the file, as an integer.
	 * @throws IOException If the version can't be retrieved for some reason.
	 */
	public static int getVersion(String filename, boolean byteorder) throws IOException
	{
		/* psStart is the pointer to the first byte in the PE data.  The byte is pointed to at 0x3c */
		int peStart;
		/* The signature of the pe file, PE\0\0 */
		int peSignature;
		/* The number of sections (.data, .text, .rsrc, etc) */
		short numberOfSections;
		/* A pointer to the "optional" header (which will always be present in .exe files */
		int ptrOptionalHeader;

		/* The file we're reading from.*/
		MappedByteBuffer file = new FileInputStream(filename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, new File(filename).length());
		/* Set the file ordering to little endian */
		file.order(ByteOrder.LITTLE_ENDIAN);

		/* The start of the PE is pointed at by the 0x3c'th byte */
		peStart = file.getInt(PE_START);

		/* The first 4 bytes are the signature */
		peSignature = file.getInt(peStart + 0);

		/* Verify that it's a valid pe file.  IF not, throw an exception */
		if(peSignature != 0x00004550)
			throw new IOException("Invalid PE file!");

		/* The number of sections is the short starting at the 6th byte */
		numberOfSections = file.getShort(peStart + 6);

		/* Get a pointer to the optional header */
		ptrOptionalHeader = peStart + 24;



		return processOptionalHeader(file, ptrOptionalHeader, numberOfSections, byteorder);
	}

	/** This reads the optional header and returns the version, or throws an IOException if the version
	 * could not be found.
	 *
	 * @param file The file we're reading from.
	 * @param ptrOptionalHeader A pointer to the optional header.
	 * @param numberOfSections The number of sections that we will need to process.
	 * @return The version, always.
	 * @throws IOException If the version couldn't be found.
	 */
	private static int processOptionalHeader(MappedByteBuffer file, int ptrOptionalHeader, int numberOfSections, boolean byteorder) throws IOException
	{
		/* Set to true if this is a PE+ file.  Some addresses are slightly different.  Untested!  */
		boolean plus;
		/* The number of RVA entries.  We don't care what the entries are, so we just skip right over them.  */
		int numberOfRvaAndSizes;
		/* A pointer to the table of sections.  This, along with the number of sections, is passed to the next
		 * function. */
		int ptrSectionTable;
		/* The version, which will eventually be returned. */
		int version;

		/* PE+ files have the first ("magic") byte set to 0x20b */
		plus = file.getShort(ptrOptionalHeader) == 0x020b;
		/* Get the RVA counts from the optional header */
		numberOfRvaAndSizes = file.getInt(ptrOptionalHeader + (plus ? 108 : 92));

		/* The optional header is 96 bytes, and each RVA is 8 bytes.  Skip over them all. */
		ptrSectionTable = ptrOptionalHeader + 96 + (numberOfRvaAndSizes * 8);

		/* Get the version from the sections */
		version = processSections(file, ptrSectionTable, numberOfSections, byteorder);

		/* If the version wasn't found, throw an exception */
		if(version == 0)
			throw new IOException("Couldn't find .rsrc section!");

		return version;

	}

	/** Step through the table of sections, looking for .rsrc.  When the .rsrc sections is found, call
	 * another function to process it.
	 *
	 * @param file The file we're reading from
	 * @param sectionsBase A pointer to the beginning of the sections.
	 * @param numberOfSections The number of sections.
	 * @return The version number, or 0 if it couldn't be found.
	 * @throws IOException If there is a problem finding the version.
	 */
	private static int processSections(MappedByteBuffer file, int sectionsBase, int numberOfSections, boolean byteorder) throws IOException
	{
		/* The location where the loaded RVA will be */
		int virtualStart;
		/* The location in the file where the data starts */
		int rawStart;
		/* The difference between the virtual start and the raw start */
		int rsrcVirtualToRaw;
		/* A pointer to the beginning of the section */
		int sectionBase;

		/* Loop over the sections */
		for(int i = 0; i < numberOfSections; i++)
		{
			/* Get the virtual address where the section starts */
			virtualStart = file.getInt(sectionsBase + (i * 40) + 12);
			/* Get the raw address where the section starts */
			rawStart = file.getInt(sectionsBase + (i * 40) + 20);
			/* Get the base of the section */
			sectionBase = sectionsBase + (i * 40);
			/* Find the difference between the actual location of rsrc and the virtual location of it */
			rsrcVirtualToRaw = rawStart - virtualStart;

			/* If we've found the rsrc section, process it.  If not, we really don't care. */
			if(file.getLong(sectionsBase + (i * 40)) == rsrc)
				return processResourceRecord(new LinkedList<Integer>(), file, 0, file.getInt(sectionBase + 20), rsrcVirtualToRaw, byteorder);
		}

		return 0;
	}

	/** This indirectly recursive function walks the resource tree by calling processEntry which, in turn,
	 * calls it.  The function looks specifically for the version section.  As soon as it finds a leaf node
	 * for a version section, it returns the data all the way back up the resursive stack.  The recursion
	 * will never go deeper than 3 levels.
	 *
	 * @param tree The "tree" up to this point -- a maximum of 3 levels.
	 * @param file The file we're processing.
	 * @param recordOffset The offset of the record that we're going to process.  It's added to the rsrcStart
	 *  value to get a pointer.
	 * @param rsrcStart The very beginning of the rsrc section.  Used as a base value.
	 * @param rsrcVirtualToRaw The value that has to be added to the virtual section to get the raw section.
	 * @return The version, or 0 if it couldn't be found.
	 * @throws IOException If there's an error finding the version.
	 */
	private static int processResourceRecord(LinkedList <Integer> tree, MappedByteBuffer file, int recordOffset, int rsrcStart, int rsrcVirtualToRaw, boolean byteorder) throws IOException
	{
		int i;

		/* The recordOffset is an offset from the start or rsrc, so calculate it as such */
		int recordAddress = recordOffset + rsrcStart;
		/* The number of name entries that we're going to have to skip over */
		short numberNameEntries = file.getShort(recordAddress + 12);
		/* The number of ID entires that we're going to have to search through */
		short numberIDEntries = file.getShort(recordAddress + 14);
		/* A pointer to the start of the ID entries, right after the name entries */
		int ptrIDEntriesBase;
		/* A pointer to the current entry we're looking at */
		int entry;
		/* Stores the version so we can check if we're finished */
		int version;

		/* The header is 16 bytes, and each name entry is 8 bytes.  Skip them. */
		ptrIDEntriesBase = recordAddress + 16  + (numberNameEntries * 8);

		/* Loop through the id entries, which come right after the name entries */
		for(i = 0; i < numberIDEntries; i++)
		{
			/* Each entry is 8 bytes, skip over the ones we've already seen */
			entry = ptrIDEntriesBase + (i * 8);
			/* Process the entry.  processEntry() will call processResourceRecord() again for branches. */
			version = processEntry(new LinkedList<Integer>(tree), file, entry, rsrcStart, rsrcVirtualToRaw, byteorder);
			/* If we've found the version, return it immediately.  Otherwise, keep looping. */
			if(version != 0)
				return version;
		}

		return 0;
	}

	/** Process an entry recursively.  If a leaf node is found, and it's the version node, return it.
	 *
	 * @param tree The list of nodes we've been to.
	 * @param file The file we're processing.
	 * @param entry A pointer to the start of the entry.
	 * @param rsrcStart A pointer to the beginning of the rsrc section.
	 * @param rsrcVirtualToRaw The conversion between the virtual and raw address.
	 * @return The version, or 0 if it wasn't found in this entry.
	 * @throws IOException If there's an error finding the version.
	 */
	private static int processEntry(LinkedList<Integer> tree, MappedByteBuffer file, int entry, int rsrcStart, int rsrcVirtualToRaw, boolean byteorder) throws IOException
	{
		/* The address of the next node, or the address of the data.  The left-most bit tells us which
		 * address this actually is. */
		int nextAddress = file.getInt(entry + 4);
		/* The version is stored in this so it can be returned. */
		int version;
		/* The size of the data */
		int dataSize;
		/* The buffer where we store the data (will be dataSize bytes) */
		byte []buffer;
		/* The address of the data within the file (converted from the RVA) */
		int rawDataAddress;

		/* Add the identifier to the tree */
		tree.addLast(file.getInt(entry + 0));

		/* Check if it's a branch by checking the left-most bit.  If it's set, it's a branch. */
		if((nextAddress & 0x80000000) != 0)
		{
			/* It's a branch, so move down to the next level. */
			version = processResourceRecord(tree, file, nextAddress & 0x7FFFFFFF, rsrcStart, rsrcVirtualToRaw, byteorder);

			/* We found the version and don't care about anything else */
			if(version != 0)
				return version;
		}
		else
		{
			/* Its a leaf, check if it's RT_VERSION.  If it is, we're done! */
			if(tree.get(0) == RT_VERSION)
			{
				/* Convert the relative address to the actual address by using rsrcVirtualToRaw */
				rawDataAddress = file.getInt(rsrcStart + nextAddress) + rsrcVirtualToRaw;
				/* Get the data size */
				dataSize = file.getInt(rsrcStart + nextAddress + 4);
				/* Allocate memory in the buffer to store the incoming data */
				buffer = new byte[dataSize];

				/* Set the position in the file to the address of the data */
				/* Get the data */
				file.position(rawDataAddress);
				file.get(buffer);

				/* Combine the data and return it. */
				if (byteorder)
					return   	(buffer[0x3C] & 0xFF) << 24 |
								(buffer[0x3E] & 0xFF) << 16 |
								(buffer[0x38] & 0xFF) << 8  |
								(buffer[0x3A] & 0xFF) << 0;
				else 
					return   	(buffer[0x3A] & 0xFF) << 24 |
								(buffer[0x38] & 0xFF) << 16 |
								(buffer[0x3E] & 0xFF) << 8  |
					    	    (buffer[0x3C] & 0xFF) << 0;
			}
		}

		return 0;
	}

	public static void main(String []args) throws IOException
	{
		/*System.out.println(String.format("%08x", getVersion("./STAR/starcraft.exe", false)));
		System.out.println(String.format("%08x", getVersion("./W2BN/Warcraft II BNE.exe", true)));
		System.out.println(String.format("%08x", getVersion("./WAR3/war3.exe", true)));
		System.out.println(String.format("%08x", getVersion("./D2DV/Game.exe", false)));
		System.out.println(String.format("%08x", getVersion("./DRTL/Diablo.exe", false)));
		System.out.println(String.format("%08x", getVersion("./DSHR/Diablo_s.exe", false)));
		System.out.println(String.format("%08x", getVersion("./JSTR/StarCraftJ.exe", false)));
		System.out.println(String.format("%08x", getVersion("./SSHR/StarCraft_s.exe", false)));
		System.out.println(String.format("%08x", getVersion("./JSTR/StarCraftJ.exe", false)));
		int tmpWAR3 = getVersion("./war3/war3.exe", true);

		int WAR3 =  (tmpWAR3 & 0xFF000000) >>> 24 |
					(tmpWAR3 & 0x00FF0000) >> 8 |
					(tmpWAR3 & 0x0000FF00) << 8 |
					(tmpWAR3 & 0x000000FF) << 24;
		System.out.println(String.format("%08x", WAR3));

		System.out.println(Constants.versionHashSTAR == getVersion("./STAR/starcraft.exe", false));
		System.out.println(Constants.versionHashW2BN == getVersion("./W2BN/Warcraft II BNE.exe", true));
		System.out.println(Constants.versionHashWAR3 == WAR3);
		System.out.println(Constants.versionHashD2DV == getVersion("./D2DV/Game.exe", false));
		System.out.println(Constants.versionHashD2XP == getVersion("./D2XP/Game.exe", false));*/
	}
}
