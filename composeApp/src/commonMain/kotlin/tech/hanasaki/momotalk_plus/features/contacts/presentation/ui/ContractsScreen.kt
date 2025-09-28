package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.features.contacts.presentation.navigation.ContactsRoute

@Composable
fun ContactsScreen(
    currentUser: UserProfile?,
    onAvatarClick: () -> Unit,
) {
    val contactsNavController = rememberNavController()

    NavHost(
        navController = contactsNavController,
        startDestination = ContactsRoute.ContactList
    ) {
        composable<ContactsRoute.ContactList> {
            ContactListPage(
                currentUser = currentUser,
                onAvatarClick = onAvatarClick,
                onNavigateToAddContact = { contactsNavController.navigate(ContactsRoute.AddContact) },
                onNavigateToContactDetail = { contactId ->
                    contactsNavController.navigate(ContactsRoute.ContactDetail(contactId))
                }
            )
        }

        composable<ContactsRoute.ContactDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ContactsRoute.ContactDetail>()
            ContactDetailPage(
                userId = route.id,
                onNavigateBack = { contactsNavController.popBackStack() }
            )
        }

        composable<ContactsRoute.AddContact> {
            AddContactPage(
                onNavigateBack = { contactsNavController.popBackStack() }
            )
        }
    }

}