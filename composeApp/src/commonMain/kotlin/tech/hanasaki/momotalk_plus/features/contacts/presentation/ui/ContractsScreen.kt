@file:OptIn(kotlin.time.ExperimentalTime::class)

package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.features.contacts.presentation.navigation.ContactsRoute

@Composable
fun ContactsScreen(
    currentUser: User?,
    onAvatarClick: () -> Unit,
    onSetBottomBarVisibility: (Boolean) -> Unit,
) {
    val contactsNavController: NavHostController = rememberNavController()

    LaunchedEffect(contactsNavController) {
        contactsNavController.currentBackStackEntryFlow.collect { entry ->
            onSetBottomBarVisibility(entry.destination.route?.contains("ContactList") == true)
        }
    }

    NavHost(
        navController = contactsNavController,
        startDestination = ContactsRoute.ContactList
    ) {
        composable<ContactsRoute.ContactList> {
            ContactListPage(
                currentUser = currentUser,
                onAvatarClick = onAvatarClick,
                onNavigateToAddContact = { contactsNavController.navigate(ContactsRoute.ManageContacts) },
                onNavigateToContactDetail = { contactId ->
                    contactsNavController.navigate(ContactsRoute.ContactDetail(contactId))
                }
            )
        }

        composable<ContactsRoute.ContactDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ContactsRoute.ContactDetail>()
            ContactDetailPage(
                userId = route.id,
                onNavigateToEditContact = { contactId ->
                    contactsNavController.navigate(ContactsRoute.EditContact(contactId))
                },
                onNavigateBack = { contactsNavController.popBackStack() }
            )
        }

        composable<ContactsRoute.EditContact> { backStackEntry ->
            val route = backStackEntry.toRoute<ContactsRoute.EditContact>()
            ContactEditPage(
                contactId = route.id,
                currentUser = currentUser,
                onNavigateBack = { contactsNavController.popBackStack() }
            )
        }

        composable<ContactsRoute.ManageContacts> {
            ContactsManagePage(
                onNavigateBack = { contactsNavController.popBackStack() }
            )
        }
    }

}