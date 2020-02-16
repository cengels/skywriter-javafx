package com.cengels.skywriter.util

import javafx.scene.control.Label

object LoremIpsum {
    val MAX = 15
}

/** Inserts the specified number of paragraphs of lorem ipsum into the text field. */
fun Label.loremIpsum(paragraphs: Int = 3) {
    if (paragraphs > 0) {
        text += "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris iaculis vel ipsum id bibendum. Cras faucibus nibh vel massa ornare, at iaculis est vehicula. Cras et scelerisque ante, vitae porttitor risus. Integer eu elit non odio dapibus aliquam in vitae ligula. Nunc convallis, ipsum quis volutpat condimentum, velit massa tempus mi, quis fringilla ligula est pharetra diam. Nulla nec nisl vel mauris sagittis dignissim. Vivamus id varius ex. Pellentesque imperdiet sit amet nisl eget finibus. Aenean in felis sodales, faucibus nunc id, malesuada ante. Proin bibendum tellus massa, at semper erat scelerisque congue. Vivamus sollicitudin dolor sed erat hendrerit fermentum. Etiam ante nisi, ultricies nec porttitor at, volutpat sit amet risus. Maecenas luctus lacus eu lectus ultrices, non condimentum libero elementum. Vestibulum lacinia imperdiet lorem vitae porttitor."
    }

    if (paragraphs > 1) {
        text += "\nInteger fringilla dui ut tellus aliquet pharetra. Pellentesque pharetra, libero id lacinia consectetur, velit ipsum faucibus nisl, eget porta elit enim vitae nibh. Aenean lobortis nisi sed fringilla lobortis. Etiam volutpat ipsum velit, auctor ornare orci egestas mattis. Duis vehicula mi vel mauris fringilla, eu mollis ante sollicitudin. Praesent velit nisl, pellentesque et ante nec, elementum porttitor neque. Sed a felis efficitur, maximus nisl vel, egestas felis. Suspendisse vitae mi eget nulla porta mollis. Cras ac ipsum tempus, gravida risus nec, condimentum nisl. Aenean pharetra, elit at posuere sollicitudin, ex lectus convallis lorem, et pretium metus quam sed ex. Maecenas efficitur enim in turpis venenatis dapibus. Nunc at rutrum lacus. Vestibulum nibh felis, consequat eget nisl et, porta lacinia massa. Vivamus luctus sapien sit amet ligula iaculis, nec suscipit lectus iaculis. Morbi finibus viverra ante, nec viverra lorem ornare quis. Fusce ligula lectus, consectetur vel faucibus at, scelerisque vitae nibh."
    }

    if (paragraphs > 2) {
        text += "\nPellentesque sed magna et velit consectetur ultrices. Vivamus eleifend nulla et fermentum laoreet. Nam suscipit consequat convallis. Maecenas et tortor orci. Sed vulputate mattis convallis. Nullam sit amet nunc augue. Aliquam erat volutpat."
    }

    if (paragraphs > 3) {
        text += "\nMorbi ac lectus ut quam sollicitudin congue. Sed sed odio risus. Integer sit amet leo nec nibh tincidunt varius sit amet sollicitudin metus. Donec tincidunt felis augue, vel varius sem molestie in. Vivamus nec elementum magna. Praesent non est massa. Mauris suscipit odio nec turpis pulvinar viverra. Curabitur et elementum elit, et pharetra eros. Sed ac justo ornare, suscipit urna ac, luctus ligula. Sed ornare odio vestibulum, maximus dolor sit amet, ultricies velit. Nullam non nisi vestibulum, volutpat lectus eget, consectetur augue. Maecenas at lectus nec purus semper consequat. Phasellus eleifend eros risus, suscipit pulvinar sem dapibus eu. Quisque laoreet libero consequat aliquet blandit. Duis quis nulla at lectus cursus cursus eleifend sit amet ex."
    }

    if (paragraphs > 4) {
        text += "Quisque laoreet aliquet mattis. Nunc suscipit leo massa, a tincidunt est faucibus vitae. Nunc facilisis mi dui, eget vehicula urna malesuada eget. Duis non augue vitae metus iaculis commodo. Duis eu commodo mi. Integer pretium nisl risus, ut venenatis ante fringilla ac. Nulla ut nunc dui. Phasellus ac leo libero. Pellentesque nec malesuada metus, eget iaculis nunc. Nulla blandit sodales mollis. Donec aliquet hendrerit risus, non interdum ipsum. Ut at faucibus risus."
    }

    if (paragraphs > 5) {
        text += "Ut pellentesque neque et pulvinar rutrum. Nam vestibulum facilisis sapien, quis aliquam libero ultricies pretium. In viverra ex neque, et commodo mi porttitor vitae. Pellentesque vel aliquam lorem, non suscipit justo. Quisque in quam placerat, cursus eros in, tempus metus. Maecenas consequat nisl eu nibh dignissim, sit amet sagittis erat fermentum. Suspendisse turpis neque, dignissim ut ultricies in, pharetra et ante. Maecenas molestie pharetra accumsan. Cras placerat suscipit mi eu accumsan. Proin et justo vel tellus tincidunt iaculis. Vivamus feugiat, lacus eu vestibulum venenatis, mauris purus vestibulum nibh, a rutrum purus ipsum in tortor."
    }

    if (paragraphs > 6) {
        text += "Pellentesque scelerisque, turpis a auctor aliquet, ipsum elit imperdiet ex, at commodo dolor felis eget magna. Nunc auctor ante nec fermentum imperdiet. Nulla vestibulum commodo libero, quis placerat risus commodo interdum. Nam bibendum sapien non pellentesque lacinia. Maecenas interdum orci nec tellus convallis, varius pretium tortor malesuada. Donec in lorem justo. Phasellus a semper leo. Quisque ac odio turpis. Phasellus vitae libero a dui vestibulum molestie vitae id libero. Praesent a erat viverra, tempor dui vitae, sagittis tellus. Maecenas non est tristique, laoreet massa ac, auctor erat. Sed interdum nisl fermentum arcu feugiat cursus. Pellentesque vehicula turpis magna, vel tristique tellus pharetra non. Proin scelerisque, nisl quis dapibus dictum, sem sem interdum velit, vel dapibus justo massa et mauris. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Donec et blandit nisi."
    }

    if (paragraphs > 7) {
        text += "Nam scelerisque orci lorem. Sed volutpat enim sit amet elit molestie vestibulum at vitae turpis. Nunc sagittis nec urna ac vestibulum. Integer faucibus risus nec viverra ornare. Curabitur pellentesque dui at neque porta consequat. Suspendisse ac ligula a diam cursus lobortis. In non hendrerit odio, vel pretium magna. Cras id metus vel libero vulputate molestie id eget elit. Proin aliquam posuere lorem, in volutpat odio luctus et. Duis bibendum auctor consequat. In eget est ac neque dignissim tempor et in neque."
    }

    if (paragraphs > 8) {
        text += "Mauris nec lobortis dolor, nec tincidunt nisl. Integer tristique, sem a molestie auctor, quam felis pharetra arcu, vel dignissim urna ipsum nec enim. Donec est lacus, vulputate eget leo sit amet, pharetra suscipit ligula. Aliquam sed lectus mattis, aliquam justo vel, ultrices ipsum. Sed elit ipsum, gravida eget placerat at, feugiat euismod nulla. Nam molestie nisl ac nunc egestas feugiat. Nulla imperdiet est eget ligula convallis consequat. Sed ornare dolor nec dignissim volutpat."
    }

    if (paragraphs > 9) {
        text += "Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Cras viverra a diam viverra porta. Pellentesque urna velit, interdum at erat non, mollis pulvinar ligula. Aliquam mollis congue nunc, ut vehicula mi fringilla sit amet. Nunc interdum, lectus eget malesuada gravida, turpis nibh cursus eros, non tempor ligula lacus sit amet eros. Etiam semper tempor purus. Aenean pretium lorem dui, eu posuere massa sagittis eu. Pellentesque in vehicula odio. Vivamus magna nisi, mollis sit amet venenatis ut, lacinia in urna. Aenean at nunc nec risus interdum porttitor. Sed bibendum risus non rutrum porta. Morbi sed laoreet libero."
    }

    if (paragraphs > 10) {
        text += "Nam vitae ullamcorper diam. Cras et velit nisl. Sed eget efficitur turpis. Aenean interdum turpis nisi, nec molestie magna consequat a. Aliquam auctor rhoncus elit a porta. Donec tortor elit, consectetur et libero non, finibus ultrices magna. Phasellus rhoncus est at ante lacinia hendrerit. Vivamus facilisis condimentum dolor nec vehicula."
    }

    if (paragraphs > 11) {
        text += "Sed eu libero eu ligula viverra efficitur id nec velit. Duis laoreet nibh vitae lobortis rutrum. Maecenas eget libero ac tellus suscipit scelerisque sit amet id est. Curabitur fringilla enim sit amet malesuada cursus. In dapibus augue id ex lacinia suscipit. Phasellus venenatis, augue sed tincidunt placerat, enim purus tincidunt nunc, aliquet finibus sapien neque at ante. Sed a justo ultricies, dictum lectus nec, accumsan tellus. Suspendisse cursus nibh leo. Suspendisse id blandit elit, nec malesuada urna. Vestibulum ante turpis, mollis eget dui eget, iaculis pretium mauris."
    }

    if (paragraphs > 12) {
        text += "Ut mattis nisl a purus bibendum, sit amet rhoncus est commodo. Etiam consequat velit at ipsum placerat consequat. Suspendisse mi quam, molestie eu tortor ut, rutrum mattis quam. Aliquam erat volutpat. Curabitur vitae neque mi. Vivamus ut enim elit. Maecenas ornare finibus sem sed rhoncus. Maecenas a est tempor, congue nulla at, tincidunt lorem. Vivamus placerat rutrum tempor. Nullam consequat hendrerit lorem, sed tincidunt tellus tempus sit amet."
    }

    if (paragraphs > 13) {
        text += "Nunc dapibus lectus nunc, a auctor metus euismod at. Nam in venenatis turpis. Nam a nisi vitae diam laoreet fermentum ut at nibh. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nam id tempor mauris. Ut imperdiet varius rutrum. Suspendisse non tempus lorem, a tincidunt lectus. Pellentesque gravida, nulla et lacinia posuere, nisl est imperdiet justo, tristique pulvinar sem neque non orci. Sed non metus viverra, eleifend diam ac, dignissim leo. Vivamus blandit rhoncus nunc in dapibus. Cras eu sapien quis velit mollis pretium ac at turpis."
    }

    if (paragraphs > 14) {
        text += "Ut sollicitudin nisi vel neque condimentum, in aliquam leo placerat. Maecenas eget elit ornare, ultricies orci eget, pretium eros. Nulla facilisi. Phasellus auctor eleifend odio, a aliquet dolor consectetur at. Etiam felis erat, maximus eu sem ut, pharetra rutrum risus. Proin ac rhoncus felis. Aenean ut quam odio."
    }

    if (paragraphs > 15) {
        text += "Praesent vehicula, nunc ac consectetur tempor, turpis lacus venenatis mi, posuere semper enim leo sit amet massa. Aenean nec est fermentum quam pretium ullamcorper sit amet ut massa. Donec sem est, maximus ullamcorper dui non, pharetra scelerisque lectus. Vestibulum sit amet magna at diam varius blandit. Integer congue elit vitae elit ultrices facilisis. Nam a congue dolor, in lacinia sapien. Nam suscipit lectus eget convallis laoreet. Ut imperdiet vestibulum mi. Suspendisse eget laoreet diam, in tempor libero. Donec sodales mi at elit varius dapibus. Nulla arcu ex, semper sit amet ex ac, pretium pulvinar tortor. Donec sem est, mollis vitae pharetra vitae, efficitur in ex. Fusce eleifend erat dolor, sit amet dictum mi egestas ut. Sed maximus mauris at mollis vulputate. Donec ac libero at arcu gravida laoreet. Etiam sapien augue, porttitor ornare consequat eu, venenatis at quam."
    }
}