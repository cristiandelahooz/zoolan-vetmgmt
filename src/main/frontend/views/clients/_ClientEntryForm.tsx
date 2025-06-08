import { useSignal } from "@vaadin/hilla-react-signals";
import {
	Button,
	ComboBox,
	DatePicker,
	EmailField,
	FormItem,
	FormLayout,
	Notification,
	PasswordField,
	TextField,
	Tooltip,
} from "@vaadin/react-components";
import type ClientRating from "Frontend/generated/com/zoolandia/app/features/client/domain/ClientRating";
import type PreferredContactMethod from "Frontend/generated/com/zoolandia/app/features/client/domain/PreferredContactMethod";
import type ReferenceSource from "Frontend/generated/com/zoolandia/app/features/client/domain/ReferenceSource";
import type Gender from "Frontend/generated/com/zoolandia/app/features/user/domain/Gender";
import { ClientServiceImpl } from "Frontend/generated/endpoints";
import handleError from "Frontend/views/_ErrorHandler";

const genders = ["MALE", "FEMALE", "OTHER"];
const contactMethods = ["SMS", "WHATSAPP", "EMAIL", "PHONE_CALL"];
const referenceSources = [
	"REFERIDO_CLIENTE",
	"REDES_SOCIALES",
	"PUBLICIDAD",
	"GOOGLE",
	"PASANTE",
	"RECOMENDACION_PROFESIONAL",
	"OTRO",
];
const ratings = ["MUY_BUENO", "BUENO", "REGULAR", "PAGO_TARDIO", "CONFLICTIVO"];

//TODO: Add Dominican Republic specific data
const provinces = [
	"Santo Domingo",
	"Santiago",
	"La Altagracia",
	"San Cristóbal",
	"La Romana",
	"Puerto Plata",
	"Luperón",
	"San Pedro de Macorís",
	"Duarte",
	"Valverde",
	"San Juan",
	"Azua",
	"Peravia",
	"Espaillat",
	"Monseñor Nouel",
	"San José de Ocoa",
	"El Seibo",
	"Monte Plata",
	"Hato Mayor",
	"Samaná",
	"Barahona",
	"Independencia",
	"Pedernales",
	"Sánchez Ramírez",
	"San Cristóbal",
	"San José de las Matas",
	"San Francisco de Macorís",
	"Constanza",
	"San Rafael del Yuma",
	"San Juan de la Maguana",
];

// TODO: Add Dominican Republic municipalities specific data
const municipalities = ["La canela", ""];
const sectors = ["Sector 1", "Sector 2"];

type ClientEntryFormProps = {
	onClientCreated?: () => void;
};

function _ClientEntryForm(props: ClientEntryFormProps) {
	// Signal declarations for each field
	const username = useSignal("");
	const password = useSignal("");
	const email = useSignal("");
	const firstName = useSignal("");
	const lastName = useSignal("");
	const phoneNumber = useSignal("");
	const birthDate = useSignal<string | undefined>(undefined);
	const gender = useSignal<Gender | undefined>(undefined);
	const nationality = useSignal("");
	const cedula = useSignal("");
	const passport = useSignal("");
	const rnc = useSignal("");
	const companyName = useSignal("");
	const preferredContactMethod = useSignal<PreferredContactMethod | undefined>(undefined);
	const additionalContactNumbers = useSignal<string[]>([]);
	const emergencyContactName = useSignal("");
	const emergencyContactNumber = useSignal("");
	const rating = useSignal<ClientRating | undefined>(undefined);
	const creditLimit = useSignal<number | undefined>(undefined);
	const paymentTermsDays = useSignal<number | undefined>(undefined);
	const notes = useSignal("");
	const referenceSource = useSignal<ReferenceSource | undefined>(undefined);
	const province = useSignal<string | undefined>(undefined);
	const municipality = useSignal<string | undefined>(undefined);
	const sector = useSignal<string | undefined>(undefined);
	const streetAddress = useSignal("");
	const referencePoints = useSignal("");
	const receivesPromotionalInfo = useSignal(true);

	const createClient = async () => {
		try {
			await ClientServiceImpl.createClient({
				// Populate required fields
				username: username.value,
				password: password.value,
				email: email.value,
				firstName: firstName.value,
				lastName: lastName.value,
				phoneNumber: phoneNumber.value,
				birthDate: birthDate.value,
				gender: gender.value,
				nationality: nationality.value,
				cedula: cedula.value, // Ensure it has 11 digits
				passport: passport.value,
				rnc: rnc.value, // Ensure it has 9 digits
				companyName: companyName.value,
				preferredContactMethod: preferredContactMethod.value,
				emergencyContactName: emergencyContactName.value,
				emergencyContactNumber: emergencyContactNumber.value, // Ensure valid phone number
				rating: rating.value,
				creditLimit: creditLimit.value,
				paymentTermsDays: paymentTermsDays.value,
				province: province.value, // Cannot be null/empty
				municipality: municipality.value, // Cannot be null/empty
				sector: sector.value, // Cannot be null/empty
				streetAddress: streetAddress.value, // Cannot be empty
				referenceSource: referenceSource.value,
				receivesPromotionalInfo: receivesPromotionalInfo.value,
			});

			// Reset form (optional)
			username.value = "";
			// Reset other fields...
			Notification.show("Client created successfully!", {
				theme: "success",
				duration: 3000,
			});
		} catch (error) {
			console.error(error);
			handleError(error);
			Notification.show("Failed to create client. Check form fields.", {
				theme: "error",
				duration: 5000,
			});
		}
	};
	return (
		<FormLayout style={{ width: "100%", flexGrow: "1" }}>
			<FormItem>
				<TextField
					placeholder="Nombre(s)"
					value={firstName.value}
					onValueChanged={(e) => {
						firstName.value = e.detail.value;
					}}
				/>
				<TextField
					placeholder="Apellido(s)"
					value={lastName.value}
					onValueChanged={(e) => {
						lastName.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				{/*TODO: add helper information about Email format*/}
				<EmailField
					placeholder="Correo Electrónico"
					value={email.value}
					onValueChanged={(e) => {
						email.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				<TextField
					placeholder="Número de teléfono"
					value={phoneNumber.value}
					maxlength={10}
					minlength={10}
					onValueChanged={(e) => {
						phoneNumber.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				<DatePicker
					placeholder="Fecha de Nacimiento"
					value={birthDate.value}
					onValueChanged={(e) => {
						birthDate.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				<ComboBox
					placeholder="Genero"
					items={genders}
					value={gender.value}
					onValueChanged={(e) => {
						gender.value = e.detail.value as Gender;
					}}
				/>
			</FormItem>
			<FormItem>
				<TextField
					placeholder="Nacionalidad"
					value={nationality.value}
					onValueChanged={(e) => {
						nationality.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				<TextField
					placeholder="Cédula"
					value={cedula.value}
					onValueChanged={(e) => {
						cedula.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				<TextField
					placeholder="Pasaporte"
					maxlength={9}
					value={passport.value}
					onValueChanged={(e) => {
						passport.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				<TextField
					placeholder="RNC"
					maxlength={9}
					value={rnc.value}
					onValueChanged={(e) => {
						rnc.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				<TextField
					placeholder="Nombre de la Empresa"
					value={companyName.value}
					onValueChanged={(e) => {
						companyName.value = e.detail.value;
					}}
				/>
			</FormItem>
			<FormItem>
				{/*TODO: Put the helper information into an information icon*/}
				<PasswordField
					placeholder="Contraseña"
					pattern="^(?=.*[0-9])(?=.*[a-zA-Z]).{8}.*$"
					errorMessage="Contraseña inválida. Debe contener al menos 8 caracteres, una letra y un número."
					value={password.value}
					onValueChanged={(e) => {
						password.value = e.detail.value;
					}}
				>
					<Tooltip slot="tooltip" text="Debe contener al menos 8 caracteres, una letra y un número" />
				</PasswordField>
			</FormItem>
			<FormItem>
				<PasswordField
					placeholder="Confirmar Contraseña"
					onValueChanged={(e) => {
						if (e.detail.value !== password.value) {
							Notification.show("La contraseña no coincide.", {
								theme: "error",
								duration: 3000,
							});
						}
					}}
					errorMessage="La contraseña no coincide"
				/>
			</FormItem>
			<FormItem>
				<ComboBox
					placeholder="Método de Contacto Preferido"
					items={contactMethods}
					value={preferredContactMethod.value}
					onValueChanged={(e) => {
						preferredContactMethod.value = e.detail.value as PreferredContactMethod;
					}}
				/>
			</FormItem>
			<FormItem>
				<ComboBox
					placeholder="¿Como nos conoció?"
					items={referenceSources}
					value={referenceSource.value}
					onValueChanged={(e) => {
						referenceSource.value = e.detail.value as ReferenceSource;
					}}
				/>
			</FormItem>
			<FormItem>
				<Button onClick={createClient} theme="primary">
					Crear Cliente
				</Button>
			</FormItem>
		</FormLayout>
	);
}

export default _ClientEntryForm;
