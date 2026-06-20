import React, {useState} from 'react';
import {
  Alert,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {CompositeNavigationProp} from '@react-navigation/native';
import {BottomTabNavigationProp} from '@react-navigation/bottom-tabs';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {parseApiError} from '../api/client';
import * as orderApi from '../api/orderApi';
import {Button} from '../components/Button';
import {Header} from '../components/Header';
import {useCart} from '../context/CartContext';
import {CartStackParamList, MainTabParamList} from '../navigation/types';
import {colors} from '../theme/colors';
import {radius, spacing} from '../theme/spacing';
import {formatCurrency} from '../utils/format';

type CheckoutNav = CompositeNavigationProp<
  NativeStackNavigationProp<CartStackParamList, 'Checkout'>,
  BottomTabNavigationProp<MainTabParamList>
>;

type Props = {
  navigation: CheckoutNav;
};

export function CheckoutScreen({navigation}: Props) {
  const {cart, refreshCart} = useCart();
  const [placing, setPlacing] = useState(false);

  const handlePlaceOrder = async () => {
    if (!cart?.items.length) {
      Alert.alert('Empty Cart', 'Add items before checkout');
      return;
    }
    setPlacing(true);
    try {
      const order = await orderApi.placeOrder();
      await refreshCart();
      Alert.alert(
        'Order Placed! 🎉',
        `Order #${order.id} confirmed. Total: ${formatCurrency(order.totalAmount)}`,
        [
          {
            text: 'View Orders',
            onPress: () => {
              const tabNav =
                navigation.getParent<BottomTabNavigationProp<MainTabParamList>>();
              tabNav?.navigate('OrdersTab', {screen: 'Orders'});
              navigation.popToTop();
            },
          },
          {
            text: 'Continue Shopping',
            onPress: () => navigation.popToTop(),
          },
        ],
      );
    } catch (err) {
      Alert.alert('Checkout Failed', parseApiError(err).message);
    } finally {
      setPlacing(false);
    }
  };

  return (
    <View style={styles.container}>
      <Header
        title="Checkout"
        subtitle="Review your order"
        showBack
        onBack={() => navigation.goBack()}
      />
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Delivery Address</Text>
          <Text style={styles.address}>Home — Deliver in 10 minutes ⚡</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Order Summary</Text>
          {cart?.items.map(item => (
            <View key={item.id} style={styles.summaryRow}>
              <Text style={styles.summaryName} numberOfLines={1}>
                {item.productName} x{item.quantity}
              </Text>
              <Text style={styles.summaryPrice}>
                {formatCurrency(item.subtotal)}
              </Text>
            </View>
          ))}
          <View style={styles.divider} />
          <View style={styles.summaryRow}>
            <Text style={styles.totalLabel}>Total Amount</Text>
            <Text style={styles.totalValue}>
              {formatCurrency(cart?.totalAmount ?? 0)}
            </Text>
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Payment</Text>
          <Text style={styles.payment}>Cash on Delivery</Text>
        </View>
      </ScrollView>

      <View style={styles.footer}>
        <Button
          title={`Place Order • ${formatCurrency(cart?.totalAmount ?? 0)}`}
          onPress={handlePlaceOrder}
          loading={placing}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: spacing.lg,
    paddingBottom: 100,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.lg,
    marginBottom: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.md,
  },
  address: {
    fontSize: 14,
    color: colors.textSecondary,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: spacing.sm,
  },
  summaryName: {
    flex: 1,
    fontSize: 14,
    color: colors.text,
    marginRight: spacing.md,
  },
  summaryPrice: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.text,
  },
  divider: {
    height: 1,
    backgroundColor: colors.border,
    marginVertical: spacing.md,
  },
  totalLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text,
  },
  totalValue: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.primary,
  },
  payment: {
    fontSize: 14,
    color: colors.textSecondary,
  },
  footer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: colors.surface,
    padding: spacing.lg,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
});
