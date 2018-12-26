package ru.fedosov.email.parser

import java.io.File
import java.util.*
import javax.mail.*
import kotlin.collections.HashMap


fun main(args: Array<String>) {

    val host = "mail.majordomo.ru"// change accordingly
    val mailStoreType = "pop3"
    val username = "anadykto@qclfarm.ru"// change accordingly
    val password = "pois0ned"// change accordingly
    val moleculeles = getMoleculesFromMail(host, mailStoreType, username, password)
    moleculeles.forEach { molecule ->
        if ((molecule.value.reCalculatePM6.size > 0 && molecule.value.reCalculateCBS.size > 0)
                || (molecule.value.reCalculatePM6.size > 0 && molecule.value.reCalculate6111.size > 0)
                || (molecule.value.reCalculateCBS.size > 0 && molecule.value.reCalculate6111.size > 0)) {
            File("./output4/${molecule.key}").bufferedWriter().use { out ->
                out.write("PM6" + Arrays.toString(molecule.value.reCalculatePM6.toArray()) + " \n")
                out.write("CBS" + Arrays.toString(molecule.value.reCalculateCBS.toArray()) + " \n")
                out.write("6111" + Arrays.toString(molecule.value.reCalculate6111.toArray()) + " \n")
            }
        }
    }
}


fun getMoleculesFromMail(host: String, storeType: String, user: String,
                         password: String): HashMap<String, Molecule> {
    try {

        //create properties field
        val properties = Properties()
        properties.put("mail.pop3.host", host)
        properties.put("mail.pop3.port", "995")
        properties.put("mail.pop3.starttls.enable", "true")
        val emailSession = Session.getDefaultInstance(properties)

        //create the POP3 store object and connect with the pop server
        val store = emailSession.getStore("pop3s")

        store.connect(host, user, password)

        //create the folder object and open it
        val emailFolder = store.getFolder("INBOX")
        emailFolder.open(Folder.READ_ONLY)

        // retrieve the messages from the folder in an array and print it
        val messages = emailFolder.messages
        println("messages.length---" + messages.size)
        val molecules = parseMails(messages)

        //close the store and folder objects
        emailFolder.close(false)
        store.close()
        return molecules
    } catch (e: NoSuchProviderException) {
        e.printStackTrace()
        return hashMapOf()
    } catch (e: MessagingException) {
        e.printStackTrace()
        return hashMapOf()
    } catch (e: Exception) {
        e.printStackTrace()
        return hashMapOf()
    }
}

fun parseMails(messages: Array<Message>): HashMap<String, Molecule> {
    val molecules = hashMapOf<String, Molecule>()
    messages.filter { it.subject.contains("finished") }.forEach {
        println("---------------------------------")
        val regex = "(File: )(.*.out)".toRegex()
        val files = regex.findAll(it.content.toString()).toList()
        files.filter { "(.*)(SEEDER)".toRegex().find(it.groupValues[2]) != null }.forEach { file ->
            val moleculeName = "(.*)(SEEDER)".toRegex().find(file.groupValues[2])!!.value
            val fileName = file.groupValues[2]
            val key = moleculeName
            val recalculateCount = getCountRecalculate(fileName)
            if (!molecules.containsKey(key)) molecules[key] = Molecule(moleculeName)
            else {
                val molecule = molecules[key]!!
                when (getMethodType(fileName)) {
                    "CBS" -> {
                        molecule.reCalculateCBS.add(recalculateCount)
                    }
                    "PM6" -> {
                        molecule.reCalculatePM6.add(recalculateCount)
                    }
                    "6111" -> {
                        molecule.reCalculate6111.add(recalculateCount)
                    }
                }
            }
        }
        System.out.println("Subj: " + it.subject)
    }
    return molecules
}

fun getCountRecalculate(file: String): Int {
    return ".cut".toRegex().findAll(file).toList().size
}

fun getMethodType(file: String): String {
    if (file.contains("CBS")) return "CBS"
    else if (file.contains("PM6")) return "PM6"
    else return "6111"
}
